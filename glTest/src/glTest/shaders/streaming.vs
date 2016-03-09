// NOTE: Unlike every other set of Solutions, all of the StreamingVBs currently use this shader.
// If you need to make modifications for a particular test, create a new shader. 
#version 450

// Input
#define POSITION    0
// Uniform
#define CB0         0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer;

// Uniforms / SSBO ----------------------------------------------------------------------------------------------------
layout(binding = CB0) uniform Cb0
{
    vec2 viewport;
} cb0;

// Input --------------------------------------------------------------------------------------------------------------
layout(location = POSITION) in vec2 inPos;

// Output -------------------------------------------------------------------------------------------------------------

// Functions ----------------------------------------------------------------------------------------------------------
void main()
{
    gl_Position = vec4(inPos * cb0.viewport + vec2(-1, 1), 0, 1);
    //gl_Position = vec4(inPos * vec2(+2.0f / 1024f, -2.0f / 768f) + vec2(-1, 1), 0, 1);
}
