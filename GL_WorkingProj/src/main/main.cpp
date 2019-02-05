#include "main.h"
#include "Utilities.h"
#include "../SysInfo/Processor.h"

#include "../textures_loader/TexLoader.h"


//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------data & settings
//------------screen

bool  g_bFullScreen = TRUE;
HWND  g_hWnd;
RECT  g_rRect;
HDC   g_hDC;
HGLRC g_hRC;
HINSTANCE g_hInstance;

int window_width;
int window_height;


//------------demo settings
bool opengl_debug_mode_enabled = true;

double last_test_time = 0.0;
double last_gpu_test_time = 0.0;

int frame_index = 0;
int current_frame_index = 0;
int next_frame_index = 0;

int NUM_ITERATIONS = 1;

const int MAX_INSTANCES = 2000;
int CURRENT_NUM_INSTANCES = MAX_INSTANCES;
const int MAX_RANDOM_COLORS = 1000;
const int NUM_UNIFORM_CHANGES_PER_DIP = 10;
int NUM_FBO_CHANGES = 200;
const int NUM_DIFFERENT_FBOS = 4;
const int NUM_SIMPLE_VERTEX_BUFFERS = 4;
const int NUM_TEXTURES_IN_COMPLEX_MATERIAL = 6;
const int TEX_ARRAY_SIZE = 10;

const int PER_INSTANCE_DATA_VECTORS = 2; //number of vec4 (pos + color)
const int INSTANCES_DATA_VECTORS = MAX_INSTANCES * PER_INSTANCE_DATA_VECTORS;
const int INSTANCES_DATA_SIZE = INSTANCES_DATA_VECTORS * sizeof(vec4);

const int UNIFORMS_INSTANCING_MAX_CONSTANTS_FOR_INSTANCING = 100; // 72;
const int UNIFORMS_INSTANCING_OBJECTS_PER_DIP = UNIFORMS_INSTANCING_MAX_CONSTANTS_FOR_INSTANCING / PER_INSTANCE_DATA_VECTORS; //max 72 constants, 2 constants per object
int UNIFORMS_INSTANCING_NUM_GROUPS = MAX_INSTANCES / UNIFORMS_INSTANCING_OBJECTS_PER_DIP;

enum TEST_TYPE
{
	SIMPLE_DIPS_TEST = 0,
	FBO_CHANGE_TEST = 1,
	SHADERS_CHANGE_TEST = 2,
	VBO_CHANGE_TEST = 3,
	ARRAY_OF_TEXTURES_TEST = 4,
	TEXTURES_ARRAY_TEST = 5,
	UNIFORMS_SIMPLE_CHANGE_TEST = 6,
	UNIFORMS_SSBO_TEST = 7,
	INSTANCING_TEST = 8,
	TOTAL_NUM_TESTS = 9
};
TEST_TYPE test_type = SIMPLE_DIPS_TEST;

enum INSTANCING_TYPE
{
	UBO_INSTANCING = 0,
	TBO_INSTANCING = 1,
	SSBO_INSTANCING = 2,
	VBO_INSTANCING = 3,
	TEXTURE_INSTANCING = 4,
	UNIFORMS_INSTANCING = 5,
	MULTI_DRAW_INDIRECT_INSTANCING = 6,
	NUM_INSTANCING_TYPES = 7
};
INSTANCING_TYPE instancing_type = UBO_INSTANCING;


//---for benchmark
bool USE_BENCHMARK_MODE = true;
int benchmark_test_group = 0;
int benchmark_step = 0;
int benchmark_cur_test = 0;
const int BENCHMARK_SKIP_N_FRAMES = 20;
const int BENCHMARK_NUM_FRAMES_PER_STEP = 500 + BENCHMARK_SKIP_N_FRAMES;

const int BENCHMARK_NUM_INSTANCES[] = { 1000, 50, 100, 200 };
const int BENCHMARK_INSTANCING_NUM_ITERATIONS = 100;

//NOTE:
//1) TOTAL_NUM_TESTS-1 means, that INSTANCING_TEST is not test itself - it is group of tests...
//2)NUM_INSTANCING_TYPES*3 means that me perform several iterations of instancing with different num_instances
const int TOTAL_NUM_BENCHMARK_TESTS = TOTAL_NUM_TESTS-1 + NUM_INSTANCING_TYPES*3;

struct TestTime
{
	double cpu_time;
	double gpu_time;
	TestTime() : cpu_time(0.0), gpu_time(0.0)
	{}
};
TestTime test_avg_time[TOTAL_NUM_BENCHMARK_TESTS];

const char *TestName[] =
{
	"SIMPLE_DIPS_TEST                ",
	"FBO_CHANGE_TEST                 ",
	"SHADERS_CHANGE_TEST             ",
	"VBO_CHANGE_TEST                 ",
	"ARRAY_OF_TEXTURES_TEST          ",
	"TEXTURES_ARRAY_TEST             ",
	"UNIFORMS_SIMPLE_CHANGE_TEST     ",
	"UNIFORMS_SSBO_TEST              ",

	"UBO_INSTANCING                  ",
	"TBO_INSTANCING                  ",
	"SSBO_INSTANCING                 ",
	"VBO_INSTANCING                  ",
	"TEXTURE_INSTANCING              ",
	"UNIFORMS_INSTANCING             ",
	"MULTI_DRAW_INDIRECT_INSTANCING  "
};

//------------time
double total_time = 0.0;

//------------camera
CCamera camera;
float zNear = 0.1f;
float zFar = 100.f;

mat4 camera_view_matrix, camera_proj_matrix, camera_view_proj_matrix;

//------------area
const float AREA_SIZE = 20.f;
const float half_box_size = 0.05f;


//------------scene geometry
struct SimpleVertex
{
	vec3 p;
	vec3 n;
	vec2 uv;
	vec4 data; //bone id (instance data offset), random

	void init(vec3 in_p, vec3 in_n, vec2 in_uv)
	{
		p = in_p; n = in_n; uv = in_uv;
		data = vec4(0.f, 0.f, 0.f, 0.f);
	};
};

//simple box geometry data
const int BOX_NUM_VERTS = 24;
const int BOX_NUM_INDICES = 36;
SimpleVertex box_vertex_buffer[BOX_NUM_VERTS];
int box_index_buffer[BOX_NUM_INDICES];


//------------geometry rendering data
VboElement geometry_vdecl[4] = { 3,0,GL_FLOAT,   3,sizeof(vec3),GL_FLOAT,   2,sizeof(vec3) * 2,GL_FLOAT,   4,sizeof(vec3) * 2 + sizeof(vec2),GL_FLOAT };
RenderElementDescription geometry_vb_desc;

GLuint ground_vao_id = 0;
GLuint ground_vbo_id = 0;
GLuint ground_ibo_id = 0;

GLuint geometry_vao_id = 0;
GLuint geometry_vbo_id = 0;
GLuint geometry_ibo_id = 0;

//complex mesh vb (consist from boxes instances)
SimpleVertex *complex_mesh_vertex_buffer = NULL;
int *complex_mesh_index_buffer = NULL;
GLuint ws_complex_geometry_vao_id = 0, ws_complex_geometry_vbo_id = 0, ws_complex_geometry_ibo_id = 0;


//------------shaders
Shader ground_shader, ground_4tex_shader;

const int NUM_DIFFERENT_SIMPLE_SHADERS = 4;
Shader simple_color_shader[NUM_DIFFERENT_SIMPLE_SHADERS]; //shaders changing test
Shader simple_geometry_shader; //for fbo, vbo changes test
Shader array_of_textures_shader, textureArray_shader; //textures changes test

//instancing types shader
Shader tex_instancing_shader, vbo_instancing_shader, ubo_instancing_shader, tbo_instancing_shader, ssbo_instancing_shader, uniforms_instancing_shader;

//change uniforms vs ubo
Shader uniforms_changes_test_shader, uniforms_changes_ssbo_shader;
GLuint ColorShader_uniformLocation[NUM_UNIFORM_CHANGES_PER_DIP];
GLuint SimpleColorShader_PosUniformLocation = -1;

//multi draw indirect
typedef  struct {
	GLuint vertexCount;
	GLuint instanceCount;
	GLuint firstVertex;
	GLuint baseVertex;
	GLuint baseInstance;
} DrawElementsIndirectCommand;
DrawElementsIndirectCommand multi_draw_indirect_buffer[MAX_INSTANCES];


//------------textures
GLuint array_of_textures[TEX_ARRAY_SIZE] = { 0 };
GLuint texture_array_id = 0;

GLuint Sampler_linear = 0;
GLuint Sampler_nearest = 0;


//------------instances data
vec4 *complex_mesh_instances_data = NULL;

vec4 *randomColors = NULL;
vec4 *all_instances_uniform_data = NULL;
GLuint instances_uniforms_ssbo = 0;


//------------instancing
GLuint dips_texture_buffer = 0, dips_texture_buffer_tex = 0;
GLuint dips_uniform_buffer = 0;
GLuint all_instances_data_vbo = 0;
GLuint ssbo_instances_data = 0;
GLuint draw_indirect_buffer = 0;
GLuint textureInstancingPBO[2] = { 0, 0 }, textureInstancingDataTex = 0;
const int NUM_INSTANCES_PER_LINE = 128;

GLuint geometry_vao_vbo_instancing_id = 0;


//------------fbo change tests
GLuint fbo_buffer[NUM_DIFFERENT_FBOS] = { 0 };
GLuint fbo_buffer_tex[NUM_DIFFERENT_FBOS] = { 0 };
GLuint fbo_buffer_depth_tex[NUM_DIFFERENT_FBOS] = { 0 };


//------------vbo change test
GLuint separate_geometry_vao_id[NUM_SIMPLE_VERTEX_BUFFERS] = { 0 };
GLuint separate_geometry_vbo_id[NUM_SIMPLE_VERTEX_BUFFERS] = { 0 };
GLuint separate_geometry_ibo_id[NUM_SIMPLE_VERTEX_BUFFERS] = { 0 };



//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------window stuff
void SizeOpenGLScreen(int width, int height)
{
	height = max(height, 1);
	window_width = width;
	window_height = height;
	glViewport(0, 0, width, height);
}

void CheckGLErrors()
{
	GLenum err;
	while ((err = glGetError()) != GL_NO_ERROR)
		fprintf(stderr, "OpenGL Error: %s\n", gluErrorString(err));
}

//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------queries
// two query buffers: front and back
unsigned int queryID[2][2];  //2 frames, 2 timers
unsigned int queryBackBuffer = 0, queryFrontBuffer = 1;

void genTimeQueries()
{
	glGenQueriesARB(2, queryID[0]);
	glGenQueriesARB(2, queryID[1]);
}

void swapTimeQueryBuffers()
{
	if (queryBackBuffer)
	{
		queryBackBuffer = 0;
		queryFrontBuffer = 1;
	}
	else
	{
		queryBackBuffer = 1;
		queryFrontBuffer = 0;
	}
}


//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------benchmark
void benchmark_write_results()
{
	FILE *file = fopen("benchmark_results.txt", "wb");
	if (!file)
		return;

	char buf[512];

//system info
	//cpu
	Processor cpu_info;
	sprintf(buf, "CPU: %s\r\n", cpu_info.GetName().c_str());
	fwrite(buf, 1, strlen(buf), file);

	//gpu
	const GLubyte* vendor = glGetString(GL_VENDOR);
	const GLubyte* renderer = glGetString(GL_RENDERER);
	const GLubyte* version = glGetString(GL_VERSION);
	sprintf(buf, "GPU: %s\r\n\r\n", renderer);
	fwrite(buf, 1, strlen(buf), file);

//base info
	sprintf(buf, "Parameters: CURRENT_NUM_INSTANCES %i   NUM_FBO_CHANGES %i   INSTANCING_NUM_ITERATIONS %i. Time in ms.\r\n", BENCHMARK_NUM_INSTANCES[0], NUM_FBO_CHANGES, BENCHMARK_INSTANCING_NUM_ITERATIONS);
	fwrite(buf, 1, strlen(buf), file);

//pring stated schanging time
	sprintf(buf, "\r\n---States changing time:\r\n");
	fwrite(buf, 1, strlen(buf), file);

	for (int i = 0; i < INSTANCING_TEST; i++)
	{
		sprintf(buf, "%s %.2f\r\n", TestName[i], (float)test_avg_time[i].cpu_time);
		fwrite(buf, 1, strlen(buf), file);
	}

//print absolute & relative states changing cost
	sprintf(buf, "\r\n---API call cost:\r\n");
	fwrite(buf, 1, strlen(buf), file);

	//subtract the cost of glDraw* itself (it is glDrawRangeElements_abs_cost)
	double glDrawRangeElements_abs_cost = max(test_avg_time[SIMPLE_DIPS_TEST].cpu_time, 0.00001f); //to prevent 0 div (if that happens somehow...)
	double glBindFramebuffer_abs_cost = test_avg_time[FBO_CHANGE_TEST].cpu_time * (double)CURRENT_NUM_INSTANCES / (double)NUM_FBO_CHANGES - glDrawRangeElements_abs_cost;
	double glUseProgram_abs_cost = test_avg_time[SHADERS_CHANGE_TEST].cpu_time - glDrawRangeElements_abs_cost;
	double glBindVertexArray_abs_cost = test_avg_time[VBO_CHANGE_TEST].cpu_time - glDrawRangeElements_abs_cost;
	double glBindTexture_abs_cost = (test_avg_time[ARRAY_OF_TEXTURES_TEST].cpu_time - glDrawRangeElements_abs_cost) / (double)NUM_TEXTURES_IN_COMPLEX_MATERIAL;
	double glUniform4fv_abs_cost = (test_avg_time[UNIFORMS_SIMPLE_CHANGE_TEST].cpu_time - glDrawRangeElements_abs_cost) / (double)NUM_UNIFORM_CHANGES_PER_DIP;

	int glDrawRangeElements_rel_cost = 100;
	int glBindFramebuffer_rel_cost = int(glBindFramebuffer_abs_cost * 100.0 / glDrawRangeElements_abs_cost);
	int glUseProgram_rel_cost = int(glUseProgram_abs_cost * 100.0 / glDrawRangeElements_abs_cost);
	int glBindVertexArray_rel_cost = int(glBindVertexArray_abs_cost * 100.0 / glDrawRangeElements_abs_cost);
	int glBindTexture_rel_cost = int(glBindTexture_abs_cost * 100.0 / glDrawRangeElements_abs_cost);
	int glUniform4fv_rel_cost = int(glUniform4fv_abs_cost * 100.0 / glDrawRangeElements_abs_cost);

	sprintf(buf, "glBindFramebuffer:               %.2f   %i%% \r\n", glBindFramebuffer_abs_cost, glBindFramebuffer_rel_cost);
	fwrite(buf, 1, strlen(buf), file);
	sprintf(buf, "glUseProgram:                    %.2f   %i%% \r\n", glUseProgram_abs_cost, glUseProgram_rel_cost);
	fwrite(buf, 1, strlen(buf), file);
	sprintf(buf, "glBindVertexArray:               %.2f   %i%% \r\n", glBindVertexArray_abs_cost, glBindVertexArray_rel_cost);
	fwrite(buf, 1, strlen(buf), file);
	sprintf(buf, "glBindTexture:                   %.2f   %i%% \r\n", glBindTexture_abs_cost, glBindTexture_rel_cost);
	fwrite(buf, 1, strlen(buf), file);
	sprintf(buf, "glDrawRangeElements:             %.2f   %i%% \r\n", glDrawRangeElements_abs_cost, glDrawRangeElements_rel_cost);
	fwrite(buf, 1, strlen(buf), file);
	sprintf(buf, "glUniform4fv:                    %.2f   %i%% \r\n", glUniform4fv_abs_cost, glUniform4fv_rel_cost);
	fwrite(buf, 1, strlen(buf), file);


//pring instancing
	sprintf(buf, "\r\n---Instancing time:\r\n");
	fwrite(buf, 1, strlen(buf), file);

	//base info
	sprintf(buf, "cpu time (gpu time)\r\n");
	fwrite(buf, 1, strlen(buf), file);

	sprintf(buf, "num instances                       %i            %i           %i\r\n", BENCHMARK_NUM_INSTANCES[1], BENCHMARK_NUM_INSTANCES[2], BENCHMARK_NUM_INSTANCES[3]);
	fwrite(buf, 1, strlen(buf), file);

	for (int i = 0; i < NUM_INSTANCING_TYPES; i++)
	{
		sprintf(buf, "%s %.2f (%.2f)   %.2f (%.2f)   %.2f (%.2f)\r\n", TestName[INSTANCING_TEST + i],
			(float)test_avg_time[INSTANCING_TEST + i].cpu_time, (float)test_avg_time[INSTANCING_TEST + i].gpu_time,
			(float)test_avg_time[INSTANCING_TEST + NUM_INSTANCING_TYPES + i].cpu_time, (float)test_avg_time[INSTANCING_TEST + NUM_INSTANCING_TYPES + i].gpu_time,
			(float)test_avg_time[INSTANCING_TEST + NUM_INSTANCING_TYPES*2 + i].cpu_time, (float)test_avg_time[INSTANCING_TEST + NUM_INSTANCING_TYPES*2 + i].gpu_time
		);
		fwrite(buf, 1, strlen(buf), file);
	}

	fclose(file);
}

void start_benchmark()
{
	USE_BENCHMARK_MODE = true;
	benchmark_cur_test = 0;
	benchmark_test_group = 0;
	benchmark_step = 0;

	NUM_ITERATIONS = 1;
	CURRENT_NUM_INSTANCES = BENCHMARK_NUM_INSTANCES[0];
	NUM_FBO_CHANGES = 200;
	test_type = SIMPLE_DIPS_TEST;
	instancing_type = UBO_INSTANCING;
}

void benchmark_mode_step()
{
	if (!USE_BENCHMARK_MODE)
		return;

	static int benchmark_num_processed_frames = 0;
	static double cpu_time_sum = 0.0;
	static double gpu_time_sum = 0.0;
	static double test_start_time = 0.0;

	//go to next benchmark test
	if (benchmark_num_processed_frames >= BENCHMARK_NUM_FRAMES_PER_STEP)
	{
		//calculate avg test time
		//test_avg_time[benchmark_cur_test] = cpu_time_sum / (double)(BENCHMARK_NUM_FRAMES_PER_STEP - BENCHMARK_SKIP_N_FRAMES);
		test_avg_time[benchmark_cur_test].cpu_time = (total_time - test_start_time) * 1000.0 / (double)(BENCHMARK_NUM_FRAMES_PER_STEP - BENCHMARK_SKIP_N_FRAMES);
		test_avg_time[benchmark_cur_test].gpu_time = gpu_time_sum / (double)(BENCHMARK_NUM_FRAMES_PER_STEP - BENCHMARK_SKIP_N_FRAMES);

		//next step
		benchmark_cur_test++;
		benchmark_step++;

		//finish benchmark
		//if (test_type == INSTANCING_TEST && instancing_type == NUM_INSTANCING_TYPES-1)
		if (benchmark_test_group >= 3 && benchmark_step >= NUM_INSTANCING_TYPES)
		{
			//resture states
			test_type = SIMPLE_DIPS_TEST;
			instancing_type = UBO_INSTANCING;
			NUM_ITERATIONS = 1;
			CURRENT_NUM_INSTANCES = 1000;
			UNIFORMS_INSTANCING_NUM_GROUPS = CURRENT_NUM_INSTANCES / UNIFORMS_INSTANCING_OBJECTS_PER_DIP;
			USE_BENCHMARK_MODE = false;
			benchmark_write_results();
			return;
		}

		//next step
		/*benchmark_cur_test++;
		test_type = static_cast<TEST_TYPE>(min(benchmark_cur_test, TOTAL_NUM_TESTS-1));
		if (test_type == INSTANCING_TEST)
		{
			instancing_type = static_cast<INSTANCING_TYPE>(min(benchmark_cur_test - INSTANCING_TEST, NUM_INSTANCING_TYPES-1));

			NUM_ITERATIONS = 100; //repeat test a lot of times
			CURRENT_NUM_INSTANCES = 144; // 36; //NOTE: to measure instancing timing make sure you are CPU bound, so - don't need to set large numbers here
			UNIFORMS_INSTANCING_NUM_GROUPS = max(CURRENT_NUM_INSTANCES / UNIFORMS_INSTANCING_OBJECTS_PER_DIP, 1);
		}*/

		if ((benchmark_test_group == 0 && benchmark_step >= INSTANCING_TEST) || (benchmark_test_group > 0 && benchmark_step >= NUM_INSTANCING_TYPES)) //go to next group
		{
			benchmark_test_group++;
			benchmark_step = 0;

			//all other groups of tests for instancing
			//NOTE: to measure instancing timing make sure you are CPU bound, so - don't need to set large numbers here
			CURRENT_NUM_INSTANCES = BENCHMARK_NUM_INSTANCES[benchmark_test_group];
			NUM_ITERATIONS = BENCHMARK_INSTANCING_NUM_ITERATIONS; //repeat test a lot of times
			UNIFORMS_INSTANCING_NUM_GROUPS = max(CURRENT_NUM_INSTANCES / UNIFORMS_INSTANCING_OBJECTS_PER_DIP, 1);
		}

		if (benchmark_test_group == 0 && benchmark_step < INSTANCING_TEST) //for first N steps perform API calls tests
			test_type = static_cast<TEST_TYPE>(benchmark_step);
		else
		{
			test_type = INSTANCING_TEST; //all other testa for instancing
			instancing_type = static_cast<INSTANCING_TYPE>(benchmark_step);
		}

		//clear timers
		benchmark_num_processed_frames = 0;
		cpu_time_sum = 0.0;
		gpu_time_sum = 0.0;
	}

	//start calculation time
	if (benchmark_num_processed_frames >= BENCHMARK_SKIP_N_FRAMES)
	{
		cpu_time_sum += last_test_time;
		gpu_time_sum += last_gpu_test_time;
	}

	if (benchmark_num_processed_frames == BENCHMARK_SKIP_N_FRAMES)
		test_start_time = total_time;

	//step
	benchmark_num_processed_frames++;
}


//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------init

//----------------------------------------------------------------------------------------------------scene geometry

void create_instance_geometry()
{
//simple box
	int i, j;

//separate attribs
	//verts
	vec3 box_iffset = vec3(0, 0, 0);
	vec3 box_verts[8] = {
		vec3(-half_box_size, -half_box_size, half_box_size),
		vec3(half_box_size, -half_box_size, half_box_size),
		vec3(half_box_size, half_box_size, half_box_size),
		vec3(-half_box_size, half_box_size, half_box_size),
		vec3(-half_box_size, -half_box_size, -half_box_size),
		vec3(half_box_size, -half_box_size, -half_box_size),
		vec3(half_box_size, half_box_size, -half_box_size),
		vec3(-half_box_size, half_box_size, -half_box_size),
	};
	for (i = 0; i < 8; i++)
		box_verts[i] += box_iffset;

	//indices
	int box_verts_i[24] = { 1, 5, 6, 2, 3, 7, 4, 0, 0, 4, 5, 1, 2, 6, 7, 3, 0, 1, 2, 3, 7, 6, 5, 4 };

	//normals
	vec3 box_face_normals[6] = {
		vec3(1, 0, 0),
		vec3(-1, 0, 0),
		vec3(0, -1, 0),
		vec3(0, 1, 0),
		vec3(0, 0, 1),
		vec3(0, 0, -1)
	};

	//uv
	vec2 uv[4] = { vec2(0.f,0.f), vec2(1.f,0.f), vec2(1.f,1.f), vec2(0.f,1.f) };

//combine to single buffer, combine vertex buffer
	for (i = 0; i<24; i++)
	{
		box_vertex_buffer[i].p = box_verts[box_verts_i[i]];
		box_vertex_buffer[i].n = box_face_normals[int(i / 4)];
		box_vertex_buffer[i].uv = uv[i % 4];
		box_vertex_buffer[i].data = vec4(0.f, 0.f, 0.f, 0.f);
	}

//index buffer
	int box_face_indices[6] = { 0, 1, 2, 0, 2, 3 };
	for (i = 0; i<6; i++)
		for (j = 0; j<6; j++)
			box_index_buffer[i * 6 + j] = box_face_indices[j] + 4 * i;

//create vao
	geometry_vb_desc.init(sizeof(SimpleVertex), 24, (void*)&box_vertex_buffer[0], GL_STATIC_DRAW, 4, &geometry_vdecl[0]);

	create_render_element(geometry_vao_id, geometry_vbo_id, geometry_vb_desc, true, geometry_ibo_id, 36, &box_index_buffer[0]);
}

void create_ground()
{
	//simple plane
	SimpleVertex vertices[4];
	memset(&vertices[0], 0, sizeof(SimpleVertex) * 4);

	const vec3 groundNormal = vec3(0.f, 1.f, 0.f);
	vertices[0].init(vec3(-AREA_SIZE, 0.f, -AREA_SIZE), groundNormal, vec2(0.f, 0.f));
	vertices[1].init(vec3(AREA_SIZE, 0.f, -AREA_SIZE), groundNormal, vec2(1.f, 0.f));
	vertices[2].init(vec3(AREA_SIZE, 0.f, AREA_SIZE), groundNormal, vec2(1.f, 1.f));
	vertices[3].init(vec3(-AREA_SIZE, 0.f, AREA_SIZE), groundNormal, vec2(0.f, 1.f));

	int indices[6] = { 0, 1, 2, 0, 2, 3 };

	RenderElementDescription desc;
	VboElement vbo_elements[4] = { 3,0,GL_FLOAT,   3,sizeof(vec3),GL_FLOAT,   2,sizeof(vec3) * 2,GL_FLOAT, 4,sizeof(vec3) * 2 + sizeof(vec2),GL_FLOAT };
	desc.init(sizeof(SimpleVertex), 4, (void*)&vertices[0], GL_STATIC_DRAW, 4, &vbo_elements[0]);

	create_render_element(ground_vao_id, ground_vbo_id, desc, true, ground_ibo_id, 6, &indices[0]);
}

void init_complex_mesh()
{
	//complex mesh
	int i, j;
	for (i = 0; i < MAX_INSTANCES; i++)
	{
		SimpleVertex *dst_v = &complex_mesh_vertex_buffer[i * BOX_NUM_VERTS];
		int *dst_i = &complex_mesh_index_buffer[i * BOX_NUM_INDICES];
		memcpy(dst_v, &box_vertex_buffer[0], sizeof(SimpleVertex) * BOX_NUM_VERTS);
		memcpy(dst_i, &box_index_buffer[0], sizeof(int) * BOX_NUM_INDICES);

		//correct vertex buffer
		vec3 random_color = vec3(rnd01(), rnd01(), rnd01()); //part random
		for (j = 0; j < BOX_NUM_VERTS; j++)
			dst_v[j].data = vec4(i, random_color.x, random_color.y, random_color.z);

		//correct index buffer
		for (j = 0; j < BOX_NUM_INDICES; j++)
			dst_i[j] += i * BOX_NUM_VERTS;
	}


	//vbo to render complex mesh. Any geometry might be presented. For test - every part of complex mesh is simple box.
	//prepare data to transfer to gpu
	SimpleVertex *ws_complex_mesh_vertex_buffer = new SimpleVertex[BOX_NUM_VERTS  * MAX_INSTANCES];
	memcpy(&ws_complex_mesh_vertex_buffer[0], &complex_mesh_vertex_buffer[0], sizeof(SimpleVertex)*BOX_NUM_VERTS  * MAX_INSTANCES);
	for (i = 0; i < MAX_INSTANCES; i++)
	{
		SimpleVertex *dst_v = &ws_complex_mesh_vertex_buffer[i * BOX_NUM_VERTS];
		for (j = 0; j < BOX_NUM_VERTS; j++)
			dst_v[j].p += vec3(complex_mesh_instances_data[i*PER_INSTANCE_DATA_VECTORS]);
	}

	RenderElementDescription ws_complex_geometry_vb_desc;
	ws_complex_geometry_vb_desc.init(sizeof(SimpleVertex), BOX_NUM_VERTS * MAX_INSTANCES, (void*)&ws_complex_mesh_vertex_buffer[0], GL_STATIC_DRAW, 4, &geometry_vdecl[0]);
	create_render_element(ws_complex_geometry_vao_id, ws_complex_geometry_vbo_id, ws_complex_geometry_vb_desc, true, ws_complex_geometry_ibo_id, BOX_NUM_INDICES * MAX_INSTANCES, &complex_mesh_index_buffer[0]);

	//additional copies of complex_geometry for tests
	for (i = 0; i < NUM_SIMPLE_VERTEX_BUFFERS; i++)
		create_render_element(separate_geometry_vao_id[i], separate_geometry_vbo_id[i], ws_complex_geometry_vb_desc, true, separate_geometry_ibo_id[i], BOX_NUM_INDICES * MAX_INSTANCES, &complex_mesh_index_buffer[0]);
}



void create_scene_geometry_and_data()
{
	create_ground();
	create_instance_geometry();

//instances data & complex mesh
	int i, j;
	complex_mesh_instances_data = new vec4[INSTANCES_DATA_VECTORS];
	memset(&complex_mesh_instances_data[0].x, 0, sizeof(vec4)*INSTANCES_DATA_VECTORS);
	for (int i = 0; i < INSTANCES_DATA_VECTORS; i += PER_INSTANCE_DATA_VECTORS)
	{
		//random pos inside area
		complex_mesh_instances_data[i] = vec4(rnd(-1.f, 1.f) * AREA_SIZE, half_box_size*0.98f, rnd(-1.f, 1.f) * AREA_SIZE, 0.f);
		//random instance color
		complex_mesh_instances_data[i + 1] = vec4(rnd01(), rnd01(), rnd01(), rnd01());
	}

//vb for complex mesh
	complex_mesh_vertex_buffer = new SimpleVertex[BOX_NUM_VERTS  * MAX_INSTANCES];
	memset(&complex_mesh_vertex_buffer[0], 0, sizeof(SimpleVertex)*BOX_NUM_VERTS  * MAX_INSTANCES);

//ib for complex mesh
	complex_mesh_index_buffer = new int[BOX_NUM_INDICES * MAX_INSTANCES];
	memset(&complex_mesh_index_buffer[0], 0, sizeof(int)*BOX_NUM_INDICES * MAX_INSTANCES);

	init_complex_mesh();

//randomColors (just some data for shaders)
	randomColors = new vec4[MAX_RANDOM_COLORS];
	memset(&randomColors[0], 0, sizeof(vec4)*MAX_RANDOM_COLORS);
	for (int i = 0; i < MAX_RANDOM_COLORS; i++)
		randomColors[i] = vec4(rnd01(), rnd01(), rnd01(), rnd01());

	//for storing all randomColors in one buffer
	all_instances_uniform_data = new vec4[MAX_INSTANCES * NUM_UNIFORM_CHANGES_PER_DIP];
	for (int i = 0; i < MAX_INSTANCES * NUM_UNIFORM_CHANGES_PER_DIP; i++)
		all_instances_uniform_data[i] = randomColors[i % MAX_RANDOM_COLORS];
}


//----------------------------------------------------------------------------------------------------shaders
void init_shaders()
{
//ground shaders
	ground_shader.programm_id = init_shader("ground_vs", "ground_ps");
	ground_shader.add_uniform("ModelViewProjectionMatrix", 16, &camera_view_proj_matrix.mat[0]);

	ground_4tex_shader.programm_id = init_shader("ground_vs", "ground_4tex_ps");
	ground_4tex_shader.add_uniform("ModelViewProjectionMatrix", 16, &camera_view_proj_matrix.mat[0]);

//simple color shaders
	char ps_shader_name[64];
	for (int i = 0; i < NUM_DIFFERENT_SIMPLE_SHADERS; i++)
	{
		sprintf(ps_shader_name, "tests/simple_geometry_color%i_ps", i + 1);
		simple_color_shader[i].programm_id = init_shader("tests/simple_geometry_vs", &ps_shader_name[0]);
		simple_color_shader[i].add_uniform("ModelViewProjectionMatrix", 16, &camera_view_proj_matrix.mat[0]);
	}

//simple geometry
	simple_geometry_shader.programm_id = init_shader("tests/simple_geometry_vs", "tests/simple_geometry_ps");
	simple_geometry_shader.add_uniform("ModelViewProjectionMatrix", 16, &camera_view_proj_matrix.mat[0]);

//textures changes test
	array_of_textures_shader.programm_id = init_shader("tests/array_of_textures_vs", "tests/array_of_textures_ps");
	array_of_textures_shader.add_uniform("ModelViewProjectionMatrix", 16, &camera_view_proj_matrix.mat[0]);
	textureArray_shader.programm_id = init_shader("tests/texture_array_vs", "tests/texture_array_ps");
	textureArray_shader.add_uniform("ModelViewProjectionMatrix", 16, &camera_view_proj_matrix.mat[0]);

//instancing types shader
	tex_instancing_shader.programm_id = init_shader("tests/instancing/texture_instancing_vs", "tests/instancing/texture_instancing_ps");
	tex_instancing_shader.add_uniform("ModelViewProjectionMatrix", 16, &camera_view_proj_matrix.mat[0]);

	vbo_instancing_shader.programm_id = init_shader("tests/instancing/vbo_instancing_vs", "tests/instancing/vbo_instancing_ps");
	vbo_instancing_shader.add_uniform("ModelViewProjectionMatrix", 16, &camera_view_proj_matrix.mat[0]);

	ubo_instancing_shader.programm_id = init_shader("tests/instancing/ubo_instancing_vs", "tests/instancing/ubo_instancing_ps");
	ubo_instancing_shader.add_uniform("ModelViewProjectionMatrix", 16, &camera_view_proj_matrix.mat[0]);
	//bind iniform buffer with instances data to shader
	ubo_instancing_shader.bind(true);
	//GLint instanceData_location3 = glGetUniformLocation(ubo_instancing_shader.programm_id, "instance_data"); //link to shader
	//glUniformBufferEXT(ubo_instancing_shader.programm_id, instanceData_location3, dips_uniform_buffer); //actually binding
	GLint uniformBlockIndex = glGetUniformBlockIndex(ubo_instancing_shader.programm_id, "UBO");
	glBindBuffer(GL_UNIFORM_BUFFER, dips_uniform_buffer);
	glBindBufferBase(GL_UNIFORM_BUFFER, 0, dips_uniform_buffer);
	glUniformBlockBinding(ubo_instancing_shader.programm_id, uniformBlockIndex, 0);
	glBindBuffer(GL_UNIFORM_BUFFER, 0);
	glUseProgram(0);

	tbo_instancing_shader.programm_id = init_shader("tests/instancing/tbo_instancing_vs", "tests/instancing/tbo_instancing_ps");
	tbo_instancing_shader.add_uniform("ModelViewProjectionMatrix", 16, &camera_view_proj_matrix.mat[0]);

	ssbo_instancing_shader.programm_id = init_shader("tests/instancing/ssbo_instancing_vs", "tests/instancing/ssbo_instancing_ps");
	ssbo_instancing_shader.add_uniform("ModelViewProjectionMatrix", 16, &camera_view_proj_matrix.mat[0]);

	uniforms_instancing_shader.programm_id = init_shader("tests/instancing/uniforms_instancing_vs", "tests/instancing/uniforms_instancing_ps");
	uniforms_instancing_shader.add_uniform("ModelViewProjectionMatrix", 16, &camera_view_proj_matrix.mat[0]);

//change uniforms vs ssbo_instances_data
	uniforms_changes_test_shader.programm_id = init_shader("tests/uniforms_changes_vs", "tests/uniforms_changes_ps");
	uniforms_changes_test_shader.add_uniform("ModelViewProjectionMatrix", 16, &camera_view_proj_matrix.mat[0]);
	char uniform_name[32];
	for (int i = 0; i < NUM_UNIFORM_CHANGES_PER_DIP; i++)
	{
		sprintf(&uniform_name[0], "color%i", i);
		ColorShader_uniformLocation[i] = glGetUniformLocation(uniforms_changes_test_shader.programm_id, &uniform_name[0]);
	}

	uniforms_changes_ssbo_shader.programm_id = init_shader("tests/uniforms_changes_ssbo_vs", "tests/uniforms_changes_ps");
	uniforms_changes_ssbo_shader.add_uniform("ModelViewProjectionMatrix", 16, &camera_view_proj_matrix.mat[0]);
}


//----------------------------------------------------------------------------------------------------buffers

void init_buffers()
{
//shader parameters
	glGenBuffers(1, &instances_uniforms_ssbo);
	glBindBuffer(GL_SHADER_STORAGE_BUFFER, instances_uniforms_ssbo);
	glBufferData(GL_SHADER_STORAGE_BUFFER, MAX_INSTANCES * NUM_UNIFORM_CHANGES_PER_DIP * sizeof(vec4), &all_instances_uniform_data[0], GL_STATIC_DRAW);
	glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, instances_uniforms_ssbo);
	glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0); // unbind

//tbo with instances data
	glGenBuffers(1, &dips_texture_buffer);
	glBindBuffer(GL_TEXTURE_BUFFER, dips_texture_buffer);
	glBufferData(GL_TEXTURE_BUFFER, INSTANCES_DATA_SIZE, &complex_mesh_instances_data[0], GL_STATIC_DRAW);
	glGenTextures(1, &dips_texture_buffer_tex);
	glBindBuffer(GL_TEXTURE_BUFFER, 0);

//uniform buffer with instances data
	glGenBuffers(1, &dips_uniform_buffer);
	glBindBuffer(GL_UNIFORM_BUFFER, dips_uniform_buffer);
	glBufferData(GL_UNIFORM_BUFFER, INSTANCES_DATA_SIZE, &complex_mesh_instances_data[0], GL_STATIC_DRAW); //uniform_buffer_data
	glBindBuffer(GL_UNIFORM_BUFFER, 0);

//vbo with instances data
	glGenBuffers(1, &all_instances_data_vbo);
	glBindBuffer(GL_ARRAY_BUFFER, all_instances_data_vbo);
	glBufferData(GL_ARRAY_BUFFER, INSTANCES_DATA_SIZE, &complex_mesh_instances_data[0], GL_STATIC_DRAW);
	glBindBuffer(GL_ARRAY_BUFFER, 0);

	//creat and patch vertex declaration
	RenderElementDescription desc;
	VboElement vbo_elements[6] = {
		3,0,GL_FLOAT,   3,sizeof(vec3),GL_FLOAT,   2,sizeof(vec3) * 2,GL_FLOAT, 4,sizeof(vec3) * 2 + sizeof(vec2),GL_FLOAT,
		4,0,GL_FLOAT,   4,sizeof(vec4),GL_FLOAT, };
	desc.init(sizeof(SimpleVertex), 24, (void*)&box_vertex_buffer[0], GL_STATIC_DRAW, 6, &vbo_elements[0]);

	glGenVertexArrays(1, &geometry_vao_vbo_instancing_id);
	glBindVertexArray(geometry_vao_vbo_instancing_id);
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, geometry_ibo_id);
	glBindBuffer(GL_ARRAY_BUFFER, geometry_vbo_id);

	//bind vertex attributes
	for (int i = 0; i < 4; i++)
	{
		glEnableVertexAttribArray(i);
		glVertexAttribPointer((GLuint)i, desc.elements[i].number_of_elements, desc.elements[i].elem_type, GL_FALSE, desc.struct_size, (GLvoid*)(desc.elements[i].pointer_offset));
	}

	//special atributes binding
	glBindBuffer(GL_ARRAY_BUFFER, all_instances_data_vbo);
	//size of per instance data (PER_INSTANCE_DATA_VECTORS = 2 - so we have to create 2 additional attributes to transfer data)
	const int per_instance_data_size = sizeof(vec4) * PER_INSTANCE_DATA_VECTORS;
	glEnableVertexAttribArray(4);
	//4th vertex attribute, has 4 floats, 0 data offset
	glVertexAttribPointer((GLuint)4, 4, GL_FLOAT, GL_FALSE, per_instance_data_size, (GLvoid*)(0));
	//tell that we will change this attribute per instance, not per vertex
	glVertexAttribDivisor(4, 1);

	glEnableVertexAttribArray(5);
	//5th vertex attribute, has 4 floats, sizeof(vec4) data offset
	glVertexAttribPointer((GLuint)5, 4, GL_FLOAT, GL_FALSE, per_instance_data_size, (GLvoid*)(sizeof(vec4)));
	//tell that we will change this attribute per instance, not per vertex
	glVertexAttribDivisor(5, 1);

	glBindVertexArray(0);
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	glBindBuffer(GL_ARRAY_BUFFER, 0);

//ssbo_instances_data with instances data
	glGenBuffers(1, &ssbo_instances_data);
	glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo_instances_data);
	glBufferData(GL_SHADER_STORAGE_BUFFER, INSTANCES_DATA_SIZE, &complex_mesh_instances_data[0], GL_STATIC_DRAW);
	glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, ssbo_instances_data);
	glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0); // unbind

//pbo, create 2 pixel buffer objects
	glGenBuffers(2, textureInstancingPBO);
	glBindBuffer(GL_PIXEL_UNPACK_BUFFER, textureInstancingPBO[0]);
	//GL_STREAM_DRAW_ARB means that we will change data every frame
	glBufferData(GL_PIXEL_UNPACK_BUFFER, INSTANCES_DATA_SIZE, 0, GL_STREAM_DRAW_ARB);
	glBindBuffer(GL_PIXEL_UNPACK_BUFFER, textureInstancingPBO[1]);
	glBufferData(GL_PIXEL_UNPACK_BUFFER, INSTANCES_DATA_SIZE, 0, GL_STREAM_DRAW_ARB);
	glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);

	//create texture where we will store instances data on gpu
	glGenTextures(1, &textureInstancingDataTex);
	glBindTexture(GL_TEXTURE_2D, textureInstancingDataTex);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_REPEAT);
	//in each line we store NUM_INSTANCES_PER_LINE object's data. 128 in our case
	//for each object we store PER_INSTANCE_DATA_VECTORS data-vectors. 2 in our case
	//GL_RGBA32F — we have float32 data
	//complex_mesh_instances_data source data of instances, if we are not going to update data in the texture
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, NUM_INSTANCES_PER_LINE * PER_INSTANCE_DATA_VECTORS, MAX_INSTANCES / NUM_INSTANCES_PER_LINE, 0, GL_RGBA, GL_FLOAT, &complex_mesh_instances_data[0]);
	glBindTexture(GL_TEXTURE_2D, 0);

//draw indirect buffer
	glGenBuffers(1, &draw_indirect_buffer);
	glBindBuffer(GL_DRAW_INDIRECT_BUFFER, draw_indirect_buffer);
	glBufferData(GL_DRAW_INDIRECT_BUFFER, MAX_INSTANCES * sizeof(DrawElementsIndirectCommand), &complex_mesh_instances_data[0], GL_STREAM_DRAW);
	glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0); // unbind

}


//----------------------------------------------------------------------------------------------------fbo and textures

void init_fbo_and_textures()
{
//fbo
	TextureInfo fbo_attachments[2];
	fbo_attachments[0].init(GL_DEPTH_COMPONENT24, GL_DEPTH_COMPONENT, GL_NEAREST, GL_NEAREST, GL_CLAMP_TO_EDGE, GL_UNSIGNED_BYTE, false, GL_DEPTH_ATTACHMENT); //GL_LINEAR
	fbo_attachments[1].init(GL_RGBA, GL_RGBA, GL_NEAREST, GL_NEAREST, GL_CLAMP_TO_EDGE, GL_UNSIGNED_BYTE, false, GL_COLOR_ATTACHMENT0); //GL_LINEAR
	for (int i = 0; i < NUM_DIFFERENT_FBOS; i++)
	{
		create_simple_fbo(fbo_buffer[i], window_width, window_height, 2, &fbo_attachments[0]);
		fbo_buffer_tex[i] = fbo_attachments[1].id;
		fbo_buffer_depth_tex[i] = fbo_attachments[1].id;
	}

//samplers
	glGenSamplers(1, &Sampler_linear);
	glSamplerParameteri(Sampler_linear, GL_TEXTURE_WRAP_S, GL_REPEAT);
	glSamplerParameteri(Sampler_linear, GL_TEXTURE_WRAP_T, GL_REPEAT);
	glSamplerParameteri(Sampler_linear, GL_TEXTURE_WRAP_R, GL_REPEAT);
	glSamplerParameteri(Sampler_linear, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glSamplerParameteri(Sampler_linear, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

	glGenSamplers(1, &Sampler_nearest);
	glSamplerParameteri(Sampler_nearest, GL_TEXTURE_WRAP_S, GL_REPEAT);
	glSamplerParameteri(Sampler_nearest, GL_TEXTURE_WRAP_T, GL_REPEAT);
	glSamplerParameteri(Sampler_nearest, GL_TEXTURE_WRAP_R, GL_REPEAT);
	glSamplerParameteri(Sampler_nearest, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	glSamplerParameteri(Sampler_nearest, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	//glSamplerParameteri(Sampler_nearest, GL_TEXTURE_MIN_LOD, 0);
	//glSamplerParameteri(Sampler_nearest, GL_TEXTURE_MAX_LOD, 999);

//textures
//array of textures
	TextureLoader tex_loader;
	char filename[64];
	for (int i = 0; i < TEX_ARRAY_SIZE; i++)
	{
		sprintf(&filename[0], "data/textures/tex%i.bmp", i);
		tex_loader.load_bmp(&filename[0]);
		create_simple_texture(array_of_textures[i], tex_loader.width, tex_loader.height, GL_RGBA, GL_RGBA, GL_LINEAR, GL_LINEAR_MIPMAP_LINEAR, GL_CLAMP_TO_EDGE, GL_UNSIGNED_BYTE, &tex_loader.data[0]);
	}

//texture array
	int tex_array_w = tex_loader.width;
	int tex_array_h = tex_loader.height;
	glGenTextures(1, &texture_array_id);
	glBindTexture(GL_TEXTURE_2D_ARRAY, texture_array_id);
	glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, GL_RGBA8, tex_array_w, tex_array_h, TEX_ARRAY_SIZE); //Allocate the storage.
	for (int i = 0; i < TEX_ARRAY_SIZE; i++)
	{
		sprintf(&filename[0], "data/textures/tex%i.bmp", i);
		tex_loader.load_bmp(&filename[0]);
		glTexSubImage3D(GL_TEXTURE_2D_ARRAY,
			0,							//Mipmap number
			0, 0, i,					//xoffset, yoffset, zoffset
			tex_array_w, tex_array_h, 1,//width, height, depth
			GL_RGBA,					//format
			GL_UNSIGNED_BYTE,			//type
			&tex_loader.data[0]);       //pointer to data
	}
	glBindTexture(GL_TEXTURE_2D, 0);
}


//----------------------------------------------------------------------------------------------------init

void Init(HWND hWnd)
{
	int i, j, k;

//init gl
	g_hWnd = hWnd;										// Assign the window handle to a global window handle
	GetClientRect(g_hWnd, &g_rRect);					// Assign the windows rectangle to a global RECT
	InitializeOpenGL(g_rRect.right, g_rRect.bottom);	// Init OpenGL with the global rect

	glext_init();
	clearDebugLog();

	bool use_debug = opengl_debug_mode_enabled && (glGetDebugMessageLogARB != NULL);
	if (use_debug)
	{
		glDebugMessageCallbackARB(&DebugCallback, NULL);
		glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB);
	}

	glEnable(GL_DEPTH_TEST);
	SizeOpenGLScreen(g_rRect.right, g_rRect.bottom);	// Setup the screen translations and viewport

//rnd
	rndInit();
	int rnd_seed = (int)timeGetTime() % 65535;
	rndSeed(rnd_seed);

//timing
	InitTimeOperation();

//queries
	genTimeQueries();

//camera
	camera.PositionCamera(10.f, 10.f, 10.f, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f);
	camera.show_framerate = false;

//geometry and data
	create_scene_geometry_and_data();

//buffers
	init_buffers();

//textures
	init_fbo_and_textures();

//shaders
	init_shaders();


//init gl states
	glDepthFunc(GL_LESS);

//benchmark
	if (USE_BENCHMARK_MODE)
		start_benchmark();

//check errors
	//CheckGLErrors();
}



void ShutDown()
{
	del_array(complex_mesh_instances_data);
	del_array(randomColors);
	del_array(all_instances_uniform_data);
	del_array(complex_mesh_vertex_buffer);
	del_array(complex_mesh_index_buffer);

	delete_render_element(geometry_vao_id, geometry_vbo_id, geometry_ibo_id);
	delete_render_element(ground_vao_id, ground_vbo_id, ground_ibo_id);
	delete_render_element(ws_complex_geometry_vao_id, ws_complex_geometry_vbo_id, ws_complex_geometry_ibo_id);
	

	glDeleteProgram(ground_shader.programm_id);
	glDeleteProgram(ground_4tex_shader.programm_id);

	for (int i=0 ; i<NUM_DIFFERENT_SIMPLE_SHADERS; i++)
		glDeleteProgram(simple_color_shader[NUM_DIFFERENT_SIMPLE_SHADERS].programm_id);
	glDeleteProgram(simple_geometry_shader.programm_id);
	glDeleteProgram(array_of_textures_shader.programm_id);
	glDeleteProgram(textureArray_shader.programm_id);

	glDeleteProgram(tex_instancing_shader.programm_id);
	glDeleteProgram(vbo_instancing_shader.programm_id);
	glDeleteProgram(ubo_instancing_shader.programm_id);
	glDeleteProgram(tbo_instancing_shader.programm_id);
	glDeleteProgram(ssbo_instancing_shader.programm_id);
	glDeleteProgram(uniforms_instancing_shader.programm_id);

	glDeleteProgram(uniforms_changes_test_shader.programm_id);
	glDeleteProgram(uniforms_changes_ssbo_shader.programm_id);

	glDeleteTextures(1, &texture_array_id);
	glDeleteTextures(TEX_ARRAY_SIZE, &array_of_textures[0]);

	glDeleteBuffers(1, &instances_uniforms_ssbo);
	glDeleteTextures(1, &dips_texture_buffer_tex);
	glDeleteBuffers(1, &dips_texture_buffer);
	glDeleteBuffers(1, &dips_uniform_buffer);
	glDeleteBuffers(1, &all_instances_data_vbo);
	glDeleteBuffers(1, &ssbo_instances_data);
	glDeleteTextures(1, &textureInstancingDataTex);
	glDeleteBuffers(2, &textureInstancingPBO[0]);
	glDeleteBuffers(1, &geometry_vao_vbo_instancing_id);

	for (int i = 0; i<NUM_SIMPLE_VERTEX_BUFFERS; i++)
		delete_render_element(separate_geometry_vao_id[i], separate_geometry_vao_id[i], separate_geometry_vao_id[i]);

	for (int i = 0; i<NUM_DIFFERENT_FBOS; i++)
		delete_simple_fbo(fbo_buffer[i], fbo_buffer_tex[i], fbo_buffer_depth_tex[i]);
}


//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------unit tests



void update_instancing_buffers(INSTANCING_TYPE buffer_type)
{
//map & update certain buffer, depending on instancing type
//just simple implementation.
//actually for better perfomance should use glMapBufferRange with GL_MAP_WRITE_BIT | GL_MAP_UNSYNCHRONIZED_BIT flags
//also should use 3ple buffering to prevent data collisions between gpu currently using data & new changing data
	float* gpu_data = NULL;
	switch (buffer_type)
	{
	case UBO_INSTANCING:
		glBindBuffer(GL_UNIFORM_BUFFER, dips_uniform_buffer); //bind buffer
		gpu_data = (float*)glMapBuffer(GL_UNIFORM_BUFFER, GL_WRITE_ONLY); //map this buffer
		memcpy(gpu_data, &complex_mesh_instances_data[0], INSTANCES_DATA_SIZE); //copy instances data
		glUnmapBuffer(GL_UNIFORM_BUFFER); //unmap, tell stat we finished copying
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		break;

	case TBO_INSTANCING:
		glBindBuffer(GL_TEXTURE_BUFFER, dips_texture_buffer);
		gpu_data = (float*)glMapBuffer(GL_TEXTURE_BUFFER, GL_WRITE_ONLY);
		memcpy(gpu_data, &complex_mesh_instances_data[0], INSTANCES_DATA_SIZE); //copy instances data
		glUnmapBuffer(GL_TEXTURE_BUFFER);
		glBindBuffer(GL_TEXTURE_BUFFER, 0);
		break;

	case SSBO_INSTANCING:
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo_instances_data);
		gpu_data = (float*)glMapBuffer(GL_SHADER_STORAGE_BUFFER, GL_WRITE_ONLY);
		memcpy(gpu_data, &complex_mesh_instances_data[0], INSTANCES_DATA_SIZE); //copy instances data
		glUnmapBuffer(GL_SHADER_STORAGE_BUFFER);
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
		break;

	case VBO_INSTANCING:
		glBindBuffer(GL_ARRAY_BUFFER, all_instances_data_vbo);
		gpu_data = (float*)glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
		memcpy(gpu_data, &complex_mesh_instances_data[0], INSTANCES_DATA_SIZE); //copy instances data
		glUnmapBuffer(GL_ARRAY_BUFFER);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		break;

	case TEXTURE_INSTANCING:
		glBindTexture(GL_TEXTURE_2D, textureInstancingDataTex);
		glBindBufferARB(GL_PIXEL_UNPACK_BUFFER, textureInstancingPBO[current_frame_index]);

		// copy pixels from PBO to texture object
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, NUM_INSTANCES_PER_LINE * PER_INSTANCE_DATA_VECTORS, MAX_INSTANCES / NUM_INSTANCES_PER_LINE, GL_RGBA, GL_FLOAT, 0);

		// bind PBO to update pixel values
		glBindBufferARB(GL_PIXEL_UNPACK_BUFFER, textureInstancingPBO[next_frame_index]);

		//http://www.songho.ca/opengl/gl_pbo.html
		// Note that glMapBufferARB() causes sync issue.
		// If GPU is working with this buffer, glMapBufferARB() will wait(stall)
		// until GPU to finish its job. To avoid waiting (idle), you can call
		// first glBufferDataARB() with NULL pointer before glMapBufferARB().
		// If you do that, the previous data in PBO will be discarded and
		// glMapBufferARB() returns a new allocated pointer immediately
		// even if GPU is still working with the previous data.
		glBufferData(GL_PIXEL_UNPACK_BUFFER, INSTANCES_DATA_SIZE, 0, GL_STREAM_DRAW_ARB);

		gpu_data = (float*)glMapBuffer(GL_PIXEL_UNPACK_BUFFER, GL_WRITE_ONLY_ARB);
		if (gpu_data)
		{
			memcpy(gpu_data, &complex_mesh_instances_data[0], INSTANCES_DATA_SIZE); // update data
			glUnmapBuffer(GL_PIXEL_UNPACK_BUFFER); //release pointer to mapping buffer
		}
		break;

	case UNIFORMS_INSTANCING:
		break;
	}
}


void simple_dips()
{
	glBindVertexArray(ws_complex_geometry_vao_id); //what geometry to render
	simple_geometry_shader.bind(); //with what shader

	//a lot of simple dips
	for (int i = 0; i < CURRENT_NUM_INSTANCES; i++)
		glDrawRangeElements(GL_TRIANGLES, i * BOX_NUM_VERTS, (i+1) * BOX_NUM_VERTS, BOX_NUM_INDICES, GL_UNSIGNED_INT, (GLvoid*)(i*BOX_NUM_INDICES*sizeof(int))); //simple dip
}

void fbo_change_test()
{
//clear fbo
	glViewport(0, 0, window_width, window_height);
	glClearColor(0.0f / 255.0f, 0.0f / 255.0f, 0.0f / 255.0f, 0.0);
	for (int i = 0; i < NUM_DIFFERENT_FBOS; i++)
	{
		glBindFramebuffer(GL_FRAMEBUFFER, fbo_buffer[i % NUM_DIFFERENT_FBOS]);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}

//prepare dip
	glBindVertexArray(ws_complex_geometry_vao_id);
	simple_geometry_shader.bind();

//bind fbo, render 1 object... repeat N times
	for (int i = 0; i < NUM_FBO_CHANGES; i++)
	{
		glBindFramebuffer(GL_FRAMEBUFFER, fbo_buffer[i % NUM_DIFFERENT_FBOS]); //bind fbo
		glDrawRangeElements(GL_TRIANGLES, i * BOX_NUM_VERTS, (i + 1) * BOX_NUM_VERTS, BOX_NUM_INDICES, GL_UNSIGNED_INT, (GLvoid*)(i*BOX_NUM_INDICES * sizeof(int))); //simple dip
	}
	glBindFramebuffer(GL_FRAMEBUFFER, 0); //set rendering to the 'screen'
}


void shaders_change_test()
{
	glBindVertexArray(ws_complex_geometry_vao_id);

	for (int i = 0; i < CURRENT_NUM_INSTANCES; i++)
	{
		simple_color_shader[i%NUM_DIFFERENT_SIMPLE_SHADERS].bind(); //bind certain shader
		glDrawRangeElements(GL_TRIANGLES, i * BOX_NUM_VERTS, (i + 1) * BOX_NUM_VERTS, BOX_NUM_INDICES, GL_UNSIGNED_INT, (GLvoid*)(i*BOX_NUM_INDICES * sizeof(int))); //simple dip
	}
}

void uniform_change_test()
{
	if (test_type == UNIFORMS_SIMPLE_CHANGE_TEST)
	{
		uniforms_changes_test_shader.bind();
		glBindVertexArray(ws_complex_geometry_vao_id);

		for (int i = 0; i < CURRENT_NUM_INSTANCES; i++)
		{
			//set uniforms for this dip
			for (int j = 0; j < NUM_UNIFORM_CHANGES_PER_DIP; j++)
				glUniform4fv(ColorShader_uniformLocation[j], 1, &randomColors[(i*NUM_UNIFORM_CHANGES_PER_DIP + j) % MAX_RANDOM_COLORS].x);

			glDrawRangeElements(GL_TRIANGLES, i * BOX_NUM_VERTS, (i + 1) * BOX_NUM_VERTS, BOX_NUM_INDICES, GL_UNSIGNED_INT, (GLvoid*)(i*BOX_NUM_INDICES * sizeof(int))); //simple dip
		}
	}
	else
	if (test_type == UNIFORMS_SSBO_TEST)
	{
	//copy data to ssbo_instances_data bufer
		if (uniforms_changes_ssbo_shader.programm_id == -1)
			return;

		glBindBuffer(GL_SHADER_STORAGE_BUFFER, instances_uniforms_ssbo);
		float *gpu_data = (float*)glMapBufferRange(GL_SHADER_STORAGE_BUFFER, 0, CURRENT_NUM_INSTANCES * NUM_UNIFORM_CHANGES_PER_DIP * sizeof(vec4), GL_MAP_WRITE_BIT | GL_MAP_UNSYNCHRONIZED_BIT);
		memcpy(gpu_data, &all_instances_uniform_data[0], CURRENT_NUM_INSTANCES * NUM_UNIFORM_CHANGES_PER_DIP * sizeof(vec4)); //copy instances data
		glUnmapBuffer(GL_SHADER_STORAGE_BUFFER);

		//bind for shader to 0 'point' (shader will read data from this 'link point')
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, instances_uniforms_ssbo);
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

	//render
		uniforms_changes_ssbo_shader.bind();
		glBindVertexArray(ws_complex_geometry_vao_id);
		static int uniformsInstancing_data_varLocation = glGetUniformLocation(uniforms_changes_ssbo_shader.programm_id, "instance_data_location");

		for (int i = 0; i < CURRENT_NUM_INSTANCES; i++)
		{
			//set parameter to sahder - where object's data located
			glUniform1i(uniformsInstancing_data_varLocation, i*NUM_UNIFORM_CHANGES_PER_DIP);
			glDrawRangeElements(GL_TRIANGLES, i * BOX_NUM_VERTS, (i + 1) * BOX_NUM_VERTS, BOX_NUM_INDICES, GL_UNSIGNED_INT, (GLvoid*)(i*BOX_NUM_INDICES * sizeof(int))); //simple dip
		}
	}
}

void textures_change_test()
{
	glBindVertexArray(ws_complex_geometry_vao_id);
	int counter = 0;

	//switch between tests
	if (test_type == ARRAY_OF_TEXTURES_TEST)
	{
		array_of_textures_shader.bind();

		for (int i = 0; i < CURRENT_NUM_INSTANCES; i++)
		{
			//bind textures for this dip
			for (int j = 0; j < NUM_TEXTURES_IN_COMPLEX_MATERIAL; j++)
			{
				glActiveTexture(GL_TEXTURE0 + j);
				glBindTexture(GL_TEXTURE_2D, array_of_textures[counter % TEX_ARRAY_SIZE]);
				glBindSampler(j, Sampler_linear);
				counter++;
			}
			glDrawRangeElements(GL_TRIANGLES, i * BOX_NUM_VERTS, (i + 1) * BOX_NUM_VERTS, BOX_NUM_INDICES, GL_UNSIGNED_INT, (GLvoid*)(i*BOX_NUM_INDICES * sizeof(int))); //simple dip
		}
	}
	else
	if (test_type == TEXTURES_ARRAY_TEST)
	{
		//bind texture aray for all dips
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D_ARRAY, texture_array_id);
		glBindSampler(0, Sampler_linear);

		//variable to tell shader - what textures uses this dip
		static int textureArray_usedTex_varLocation = glGetUniformLocation(textureArray_shader.programm_id, "used_textures_i");
		textureArray_shader.bind();

		float used_textures_i[6];
		for (int i = 0; i < CURRENT_NUM_INSTANCES; i++)
		{
			//fill data - what textures uses this dip
			for (int j = 0; j < 6; j++)
			{
				used_textures_i[j] = counter % TEX_ARRAY_SIZE;
				counter++;
			}
			glUniform1fv(textureArray_usedTex_varLocation, 6, &used_textures_i[0]); //transfer to shader, tell what textures this material uses
			glDrawRangeElements(GL_TRIANGLES, i * BOX_NUM_VERTS, (i + 1) * BOX_NUM_VERTS, BOX_NUM_INDICES, GL_UNSIGNED_INT, (GLvoid*)(i*BOX_NUM_INDICES * sizeof(int))); //simple dip
		}
	}
}

void vbo_change_test()
{
	simple_geometry_shader.bind();

	for (int i = 0; i < CURRENT_NUM_INSTANCES; i++)
	{
		glBindVertexArray(separate_geometry_vao_id[i % NUM_SIMPLE_VERTEX_BUFFERS]); //change vbo
		glDrawRangeElements(GL_TRIANGLES, i * BOX_NUM_VERTS, (i + 1) * BOX_NUM_VERTS, BOX_NUM_INDICES, GL_UNSIGNED_INT, (GLvoid*)(i*BOX_NUM_INDICES * sizeof(int))); //simple dip
	}
}



void instancing_test(bool need_update_data = false, bool need_update_indirect_buffer = false)
{
	//update data on gpu
	if (need_update_data)
		update_instancing_buffers(instancing_type);

	//switch between instancing types
	switch (instancing_type)
	{
	case TEXTURE_INSTANCING:
		//bind texture with instances data
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, textureInstancingDataTex);
		glBindSampler(0, Sampler_nearest);

		glBindVertexArray(geometry_vao_id); //what geometry to render
		tex_instancing_shader.bind(); //with waht shader

		//tell shader texture with data located, what name it has
		static GLint location = glGetUniformLocation(tex_instancing_shader.programm_id, "s_texture_0");
		if (location >= 0)
			glUniform1i(location, 0);

		//render group of objects
		glDrawElementsInstanced(GL_TRIANGLES, BOX_NUM_INDICES, GL_UNSIGNED_INT, NULL, CURRENT_NUM_INSTANCES);
		break;

	case VBO_INSTANCING:
		vbo_instancing_shader.bind();
		//our vertex buffer wit modified vertex declaration (vdecl)
		glBindVertexArray(geometry_vao_vbo_instancing_id);
		glDrawElementsInstanced(GL_TRIANGLES, BOX_NUM_INDICES, GL_UNSIGNED_INT, NULL, CURRENT_NUM_INSTANCES);
		break;

	case UBO_INSTANCING:
		//NOTE: UBO already linked to shader. Look at ubo_instancing_shader creation
		ubo_instancing_shader.bind();
		glBindVertexArray(geometry_vao_id);
		glDrawElementsInstanced(GL_TRIANGLES, BOX_NUM_INDICES, GL_UNSIGNED_INT, NULL, CURRENT_NUM_INSTANCES);
		break;

	case TBO_INSTANCING:
		tbo_instancing_shader.bind();

		//bind to shader as special texture
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_BUFFER, dips_texture_buffer_tex);
		glTexBuffer(GL_TEXTURE_BUFFER, GL_RGBA32F, dips_texture_buffer);

		glBindVertexArray(geometry_vao_id);
		glDrawElementsInstanced(GL_TRIANGLES, BOX_NUM_INDICES, GL_UNSIGNED_INT, NULL, CURRENT_NUM_INSTANCES);
		break;

	case SSBO_INSTANCING:
		if (ssbo_instancing_shader.programm_id == -1)
			break;
		//bind ssbo_instances_data, link to shader at '0 binding point'
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo_instances_data);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo_instances_data);
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

		ssbo_instancing_shader.bind();
		glBindVertexArray(geometry_vao_id);
		glDrawElementsInstanced(GL_TRIANGLES, BOX_NUM_INDICES, GL_UNSIGNED_INT, NULL, CURRENT_NUM_INSTANCES);
		glBindVertexArray(0);
		break;

	case UNIFORMS_INSTANCING:
		uniforms_instancing_shader.bind();
		glBindVertexArray(geometry_vao_id);

		//variable - where in shader our array of uniforms located. We will write data to this array
		static int uniformsInstancing_data_varLocation = glGetUniformLocation(uniforms_instancing_shader.programm_id, "instance_data");

		//instances data might be writtet with just one call if  there are enought vectors.
		//Just for clarity, divide into groups, because usually much more there are much more data than available uniforms.
		for (int i = 0; i < UNIFORMS_INSTANCING_NUM_GROUPS; i++)
		{
			//write data to uniforms
			glUniform4fv(uniformsInstancing_data_varLocation, UNIFORMS_INSTANCING_MAX_CONSTANTS_FOR_INSTANCING, &complex_mesh_instances_data[i*UNIFORMS_INSTANCING_MAX_CONSTANTS_FOR_INSTANCING].x);

			glDrawElementsInstanced(GL_TRIANGLES, BOX_NUM_INDICES, GL_UNSIGNED_INT, NULL, UNIFORMS_INSTANCING_OBJECTS_PER_DIP);
		}
		break;

	case MULTI_DRAW_INDIRECT_INSTANCING:
		if (need_update_indirect_buffer)
		{
			//NOTE: seems data transfering is not bottle neck

			//fill indirect buffer with dips information. Just simple array
			for (int i = 0; i < CURRENT_NUM_INSTANCES; i++)
			{
				multi_draw_indirect_buffer[i].vertexCount = BOX_NUM_INDICES;
				multi_draw_indirect_buffer[i].instanceCount = 1;
				multi_draw_indirect_buffer[i].firstVertex = i*BOX_NUM_INDICES;
				multi_draw_indirect_buffer[i].baseVertex = 0;
				multi_draw_indirect_buffer[i].baseInstance = 0;
			}
			//transfer data to gpu indirect buffer
			glBindBuffer(GL_DRAW_INDIRECT_BUFFER, draw_indirect_buffer); //bind buffer
			//do to - actually, discarding data each frame is bad practice. Normal way is to use 3ple buffering approach. So, just for simplicity
			float *gpu_data = (float*)glMapBufferRange(GL_DRAW_INDIRECT_BUFFER, 0, CURRENT_NUM_INSTANCES * sizeof(DrawElementsIndirectCommand), GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
			memcpy(gpu_data, &multi_draw_indirect_buffer[0], CURRENT_NUM_INSTANCES * sizeof(DrawElementsIndirectCommand)); //copy instances data
			glUnmapBuffer(GL_DRAW_INDIRECT_BUFFER); //unmap, tell stat we finished copying
		}

		glBindBuffer(GL_DRAW_INDIRECT_BUFFER, draw_indirect_buffer);
		glBindVertexArray(ws_complex_geometry_vao_id);
		simple_geometry_shader.bind();

		glMultiDrawElementsIndirect(GL_TRIANGLES,
			GL_UNSIGNED_INT,
			NULL, //(GLvoid*)&multi_draw_indirect_buffer[0], //our information about dips
			CURRENT_NUM_INSTANCES, //number of dips
			0);
		break;
		glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);
	}
	glBindVertexArray(0);
}



//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------render
void process_key(int key)
{
	switch(key)
	{
	case VK_NUMPAD1:
	case VK_NUMPAD2:
	case VK_NUMPAD3:
	case VK_NUMPAD4:
	case VK_NUMPAD5:
	case VK_NUMPAD6:
	case VK_NUMPAD7:
	case VK_NUMPAD8:
	case VK_NUMPAD9:
		test_type = TEST_TYPE(key - VK_NUMPAD1);
		break;

	case '1':
	case '2':
	case '3':
	case '4':
	case '5':
	case '6':
	case '7':
		instancing_type = INSTANCING_TYPE(key - '1');
		break;

	case VK_F1:
		CURRENT_NUM_INSTANCES = 2000;
		UNIFORMS_INSTANCING_NUM_GROUPS = CURRENT_NUM_INSTANCES / UNIFORMS_INSTANCING_OBJECTS_PER_DIP;
		NUM_FBO_CHANGES = 400;
		break;
	case VK_F2:
		CURRENT_NUM_INSTANCES = 1000;
		UNIFORMS_INSTANCING_NUM_GROUPS = CURRENT_NUM_INSTANCES / UNIFORMS_INSTANCING_OBJECTS_PER_DIP;
		NUM_FBO_CHANGES = 200;
		break;
	case VK_F3:
		CURRENT_NUM_INSTANCES = 500;
		UNIFORMS_INSTANCING_NUM_GROUPS = CURRENT_NUM_INSTANCES / UNIFORMS_INSTANCING_OBJECTS_PER_DIP;
		NUM_FBO_CHANGES = 100;
		break;
	case VK_F4:
		CURRENT_NUM_INSTANCES = 100;
		UNIFORMS_INSTANCING_NUM_GROUPS = CURRENT_NUM_INSTANCES / UNIFORMS_INSTANCING_OBJECTS_PER_DIP;
		NUM_FBO_CHANGES = 25;
		break;
	};
}

void RenderScene()
{
	int i, j, k;

//time
	static double lastTime = timeGetTime() * 0.001;
	double timeleft = 0.0;
	double currentTime = timeGetTime() * 0.001;
	timeleft = currentTime - lastTime;
	lastTime = currentTime;
	total_time += timeleft;

//frame
	frame_index++;
	current_frame_index = frame_index % 2;
	next_frame_index = (frame_index + 1) % 2;

//start gpu time calculation
	glQueryCounter(queryID[queryBackBuffer][0], GL_TIMESTAMP); //get gpu time

//benchmark
	benchmark_mode_step();

//camera
	camera.Update();
	camera_view_matrix.look_at(camera.Position(), camera.View(), camera.UpVector());
	camera_proj_matrix.perspective(45.f, (float)window_width / (float)window_height, zNear, zFar);
	camera_view_proj_matrix = camera_proj_matrix * camera_view_matrix;

//RENDER
	glBindFramebuffer(GL_FRAMEBUFFER, 0);
	glViewport(0, 0, window_width, window_height);
	glClearColor(110.0f / 255.0f, 149.0f / 255.0f, 224.0f / 255.0f, 0.0);
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	glEnable(GL_DEPTH_TEST);
	glDisable(GL_CULL_FACE);
	glDisable(GL_BLEND);


//render ground (just a plane)
	if (test_type != FBO_CHANGE_TEST)
	{
		ground_shader.bind();
		glBindVertexArray(ground_vao_id);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, NULL);
		glBindVertexArray(0);
	}

//tests
	//calculate time spend on cpu side
	Timer timer;
	timer.StartTiming();

	for (i = 0; i < NUM_ITERATIONS; i++) //make some iterations if we need it
	{
		//switch between tests
		switch (test_type)
		{
		case SIMPLE_DIPS_TEST:
			simple_dips();
			break;

		case FBO_CHANGE_TEST:
			fbo_change_test();

			//just simple shou on ground the result... with special shader
			for (int i = 0; i < NUM_DIFFERENT_FBOS; i++)
			{
				glActiveTexture(GL_TEXTURE0 + i);
				glBindTexture(GL_TEXTURE_2D, fbo_buffer_tex[0]);
				glBindSampler(0, Sampler_linear);
			}
			ground_4tex_shader.bind();
			glBindVertexArray(ground_vao_id);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, NULL);
			glBindVertexArray(0);
			break;

		case SHADERS_CHANGE_TEST:
			shaders_change_test();
			break;

		case VBO_CHANGE_TEST:
			vbo_change_test();
			break;

		case ARRAY_OF_TEXTURES_TEST:
		case TEXTURES_ARRAY_TEST:
			textures_change_test();
			break;

		case UNIFORMS_SIMPLE_CHANGE_TEST:
		case UNIFORMS_SSBO_TEST:
			uniform_change_test();
			break;

		case INSTANCING_TEST:
			instancing_test(false, i == 0);
			break;
		}
	}

//cpu frame time. NOTE: seems it is not correct to measure just performing tests time.
	//More correct is to measure whole frame time. Even if there are some overheads (fbo bind & clear, some gl api commands like glEnable()). Because a lot of work might be done in glFlush().
	//But to make suggestions about API call cost we must be sure that bottle neck is still CPU. Otherwise this information is useless.
	static double avg_test_time = 0.0;
	last_test_time = timer.TimeElapsedInMS(); //get time
	avg_test_time += last_test_time;

//get gpu time
	glQueryCounter(queryID[queryBackBuffer][1], GL_TIMESTAMP); //get gpu time. NOTE that we use queryBackBuffer but take result from prev frame

	GLuint64 startTime, stopTime;
	glGetQueryObjectui64v(queryID[queryFrontBuffer][0], GL_QUERY_RESULT, &startTime); //take result from prev frame (use queryFrontBuffer)
	glGetQueryObjectui64v(queryID[queryFrontBuffer][1], GL_QUERY_RESULT, &stopTime);
	last_gpu_test_time = (stopTime - startTime) / 1000000.0;
	swapTimeQueryBuffers();

	//char str[128];
	//sprintf(str, "Time spent on the GPU: %f ms\n", (stopTime - startTime) / 1000000.0);
	//SetWindowText(g_hWnd, str);

//write fps & timing
	static int framesPerSecond = 0;
	static double time = 0.0;
	framesPerSecond++;
	time += timeleft;

	if (time > 1.0) //update once per second
	{
		static char strFrameRate[128] = { 0 };
		float time_in_ms = 1000.f / (float)framesPerSecond;
		avg_test_time = avg_test_time / (double)framesPerSecond;
		const char *postfix = USE_BENCHMARK_MODE ? "   (benchmark mode)" : "";
		sprintf(strFrameRate, "FPS: %d (%.4f ms). Test time %.4f ms %s", framesPerSecond, time_in_ms, (float)avg_test_time, postfix);
		SetWindowText(g_hWnd, strFrameRate);
		time -= 1.0;
		framesPerSecond = 0;
	}


//flush
	glFlush();
	SwapBuffers(g_hDC);
}