#version 450

// Attributes
#define POSITION    0
#define COLOR       1
#define DRAW_ID     2

// Uniform
#define TRANSFORM0  0
#define TRANSFORM1  1

// Storage
#define TRANSFORM   0

// Interface
#define BLOCK   0

// Options
#define USE_SHADER_DRAW_PARAMETERS  1

#ifdef USE_SHADER_DRAW_PARAMETERS
    #extension GL_ARB_shader_draw_parameters : require
#endif
#extension GL_ARB_shader_storage_buffer_object : require

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer;

// Input -----------------------------------------------------------------------
layout (location = POSITION) in vec3 inPos;
layout (location = COLOR) in vec3 inColor;
#if USE_SHADER_DRAW_PARAMETERS == 0
    layout (location = DRAW_ID) in int inDrawID;
#endif

// Uniforms / SSBO -------------------------------------------------------------
layout (location = TRANSFORM0) uniform mat4 viewProjection;
layout (binding = TRANSFORM) buffer Transforms
{
    mat4 world[];
} t1;

// Output ----------------------------------------------------------------------
layout (location = BLOCK) out Block 
{
    vec3 color;
} outBlock;

// Functions -------------------------------------------------------------------
void main()
{
    #ifdef USE_SHADER_DRAW_PARAMETERS
        mat4 world = t1.world[gl_DrawIDARB];
    #else
        mat4 world = t1.world[inDrawID];
    #endif
    vec3 worldPos = vec3(world * vec4(inPos, 1));
    gl_Position = viewProjection * vec4(worldPos, 1);
    outBlock.color = inColor;
}
