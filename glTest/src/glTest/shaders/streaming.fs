// NOTE: Unlike every other set of Solutions, all of the StreamingVBs currently use this shader.
// If you need to make modifications for a particular test, create a new shader. 
#version 450

// Output
#define FRAG_COLOR  0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer;

// Uniforms / SSBO ----------------------------------------------------------------------------------------------------

// Input --------------------------------------------------------------------------------------------------------------

// Output -------------------------------------------------------------------------------------------------------------
layout(location = FRAG_COLOR) out vec4 outColor;

// Functions ----------------------------------------------------------------------------------------------------------
void main()
{
    outColor = vec4(1, 1, 1, 1);
}
