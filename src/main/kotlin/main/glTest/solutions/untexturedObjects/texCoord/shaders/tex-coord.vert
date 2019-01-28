#version 450

// Attributes
#define POSITION    0
#define COLOR       1
#define DRAW_ID     2

// Uniform
#define TRANSFORM0  0

// Storage
#define TRANSFORM0_ 0

// Interface
#define BLOCK   0

// Requirements
#extension GL_ARB_shader_storage_buffer_object : require

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer;

// Input -----------------------------------------------------------------------
layout (location = POSITION) in vec3 inPos;
layout (location = COLOR) in vec3 inColor;
layout (location = 2) in vec4 inWorldR0;
layout (location = 3) in vec4 inWorldR1;
layout (location = 4) in vec4 inWorldR2;
layout (location = 5) in vec4 inWorldR3;

// Uniforms / SSBO -------------------------------------------------------------
layout (location = TRANSFORM0) uniform mat4 viewProjection;

// Output ----------------------------------------------------------------------
layout (location = BLOCK) out Block 
{
    vec3 color;
} outBlock;

// Functions -------------------------------------------------------------------
void main()
{
    mat4 world = mat4(inWorldR0, inWorldR1, inWorldR2, inWorldR3);
    vec3 worldPos = vec3(world * vec4(inPos, 1));
    gl_Position = viewProjection * vec4(worldPos, 1);
    outBlock.color = inColor;
}
