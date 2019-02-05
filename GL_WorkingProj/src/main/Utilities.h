#ifndef _UTILITIES_H
#define _UTILITIES_H


#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#include <process.h>
#include <mmsystem.h>
#include <math.h>
#include <gl\gl.h>										// Header File For The OpenGL32 Library
#include <gl\glu.h>										// Header File For The GLu32 Library

#include "../glext/glext.h"


#define del_it(a) if (a) { delete a; a = NULL; }

//------------------------------------------------------------------------------------------------------------------------------------------------------------------------textures
struct TextureInfo
{
	GLuint id;
	GLenum internal_format;
	GLenum format;
	GLint filtration1, filtration2;
	GLint wrap;
	GLenum type;
	bool shadow_texture;

	GLenum fbo_attachment;

	void init(GLenum in_internal_format, GLenum in_format, GLint in_filtration1, GLint in_filtration2, GLint in_wrap, GLint in_type, bool in_shadow_texture, GLenum in_fbo_attachment)
	{
		internal_format = in_internal_format;
		format = in_format;
		filtration1 = in_filtration1;
		filtration2 = in_filtration2;
		wrap = in_wrap;
		type = in_type;
		shadow_texture = in_shadow_texture;
		fbo_attachment = in_fbo_attachment;
	}
};

void create_simple_fbo(GLuint &fbo_id, int width, int height, int num_attachments, TextureInfo *tex_attachments);
void delete_simple_fbo(GLuint &fbo_id, GLuint &fbo_tex_id, GLuint &fbo_depth_tex_id);

//------------------------------------------------------------------------------------------------------------------------------------------------------------------------vertex buffer
const int VBO_STRUCT_MAX_ELEMENTS = 10;

//describes shader vertex declaration element
struct VboElement
{
	int number_of_elements;
	int pointer_offset;
	GLenum elem_type;
};

//structure that contains Vertex buffer data, description
struct RenderElementDescription
{
	int struct_size;
	int num_verts;
	void *data_pointer;

	GLenum vbo_usage;
	VboElement elements[VBO_STRUCT_MAX_ELEMENTS];
	int num_vbo_elements;

	void init(int in_struct_size, int in_num_verts, void* in_data_pointer, GLenum in_vbo_usage, int in_num_vbo_elements, VboElement *in_elements)
	{
		struct_size = in_struct_size;
		num_verts = in_num_verts;
		data_pointer = in_data_pointer;
		vbo_usage = in_vbo_usage;
		num_vbo_elements = in_num_vbo_elements;
		for (int i = 0; i < num_vbo_elements; i++)
		{
			elements[i].number_of_elements = in_elements[i].number_of_elements;
			elements[i].pointer_offset = in_elements[i].pointer_offset;
			elements[i].elem_type = in_elements[i].elem_type;
		}
	}
};


//------------------------------------------------------------------------------------------------------------------------------------------------------shaders
const int MAX_SHADER_UNIFORMS = 10;
enum ShaderUniformVariableType
{
	FLOAT_UNIFORM_TYPE,
	INT_UNIFORM_TYPE,
};
struct ShaderUniformVariable
{
	ShaderUniformVariable() : type(FLOAT_UNIFORM_TYPE), dimmension(4), address(NULL), location(-1), count(1)
	{}
	ShaderUniformVariableType type;
	int dimmension;
	void *address;
	GLuint location;
	int count;
};

struct Shader
{
	Shader() : programm_id(-1), num_uniforms(0)
	{}
	~Shader()
	{
	}
	GLuint add_uniform(const char* name, int size, void *address, ShaderUniformVariableType type = FLOAT_UNIFORM_TYPE, int num_elements = 1)
	{
		uniform_variables[num_uniforms].type = type;
		uniform_variables[num_uniforms].dimmension = size;
		uniform_variables[num_uniforms].address = address;
		uniform_variables[num_uniforms].location = glGetUniformLocation(programm_id, name);
		uniform_variables[num_uniforms].count = num_elements;
		num_uniforms++;
		return uniform_variables[num_uniforms].location;
	}
	void bind(bool just_bind = false)
	{
		glUseProgram(programm_id);

		if (just_bind)
			return;

		for (int i = 0; i < num_uniforms; i++)
		{
			switch (uniform_variables[i].dimmension)
			{
			case 1:
				if (uniform_variables[i].type == FLOAT_UNIFORM_TYPE)
					glUniform1f(uniform_variables[i].location, *static_cast<GLfloat*>(uniform_variables[i].address));
				else
					glUniform1i(uniform_variables[i].location, *static_cast<GLint*>(uniform_variables[i].address));
				break;
			case 2:
				glUniform2fv(uniform_variables[i].location, uniform_variables[i].count, static_cast<GLfloat*>(uniform_variables[i].address));
				break;
			case 3:
				glUniform3fv(uniform_variables[i].location, uniform_variables[i].count, static_cast<GLfloat*>(uniform_variables[i].address));
				break;
			case 4:
				glUniform4fv(uniform_variables[i].location, uniform_variables[i].count, static_cast<GLfloat*>(uniform_variables[i].address));
				break;
			case 9:
				glUniformMatrix3fv(uniform_variables[i].location, 1, false, static_cast<GLfloat*>(uniform_variables[i].address));
				break;
			case 16:
				glUniformMatrix4fv(uniform_variables[i].location, 1, false, static_cast<GLfloat*>(uniform_variables[i].address));
				break;
			}
		}
	}

	GLuint programm_id;
	ShaderUniformVariable uniform_variables[MAX_SHADER_UNIFORMS];
	int num_uniforms;
};

//------------------------------------------------------------------------------------------------------------------------------------------------------------------------other
//create texture
void create_simple_texture(GLuint &tex_id, int width, int height, GLenum internal_format, GLenum format, GLint filtration1, GLint filtration2, GLint wrap, GLenum type, void *data);

//create VAO, vertex array
void create_render_element(GLuint &vao_id,
	GLuint &vbo_id, RenderElementDescription &desc,
	bool use_ibo, GLuint &ibo_id, int num_indices, int *indices,
	bool stop_vao_creation = false);
void delete_render_element(GLuint &vao_id, GLuint &vbo_id, GLuint &ibo_id);

//shader
GLuint init_shader(const char* vertex_shader_file, const char* frag_shader_file, const char* geometry_shader_file = NULL, bool call_link_shader = true);
void link_shader(GLuint shaderProgram);

//debug
void clearDebugLog();
void CALLBACK DebugCallback(unsigned int source, unsigned int type, unsigned int id,
	unsigned int severity, int length,
	const char* message, const void* userParam);

#endif