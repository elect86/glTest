#version 450

#extension GL_ARB_shader_draw_parameters : require
#extension GL_ARB_shader_storage_buffer_object : require

#include "semantic.glsl"


// Input -----------------------------------------------------------------------
layout (location = POSITION) in vec3 inPos;
layout (location = COLOR) in vec3 inColor;

// Uniforms / SSBO -------------------------------------------------------------
layout (location = TRANSFORM0) uniform mat4 viewProjection;
layout (binding = CONSTANT) buffer Transforms
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
    mat4 world = t1.world[gl_DrawIDARB];
    vec3 worldPos = vec3(world * vec4(inPos, 1));
    gl_Position = viewProjection * vec4(worldPos, 1);
    outBlock.color = inColor;
}
