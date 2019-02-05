#version 450

// Requirements
#extension GL_ARB_shader_storage_buffer_object : require

#include "semantic.glsl"

// Input -----------------------------------------------------------------------
layout (location = POSITION) in vec3 inPos;
layout (location = COLOR) in vec3 inColor;
layout (location = R0) in vec4 inWorldR0;
layout (location = R1) in vec4 inWorldR1;
layout (location = R2) in vec4 inWorldR2;
layout (location = R3) in vec4 inWorldR3;

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