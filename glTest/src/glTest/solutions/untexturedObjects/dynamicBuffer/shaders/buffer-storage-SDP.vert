#version 450

// Attributes
#define POSITION    0
#define COLOR       1

// Uniform
#define TRANSFORM0  0

// Storage
#define TRANSFORM0_ 0

// Interface
#define BLOCK   0

// Requirements
#extension GL_ARB_shader_draw_parameters : require
#extension GL_ARB_shader_storage_buffer_object : require

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer;

// Input -----------------------------------------------------------------------
layout (location = POSITION) in vec3 inPos;
layout (location = COLOR) in vec3 inColor;

// Uniforms / SSBO -------------------------------------------------------------
layout (location = TRANSFORM0) uniform mat4 viewProjection;
layout (binding = TRANSFORM0_) buffer Transform
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
    vec3 worldPos = vec3(t1.world[gl_DrawIDARB] * vec4(inPos, 1));
    gl_Position = viewProjection * vec4(worldPos, 1);
    outBlock.color = inColor;
}
