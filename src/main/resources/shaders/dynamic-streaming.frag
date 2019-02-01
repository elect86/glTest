// NOTE: Unlike every other set of Solutions, all of the StreamingVBs currently use this shader.
// If you need to make modifications for a particular test, create a new shader. 
#version 420    // ARB_shading_language_420pack

#define FRAG_COLOR  0

// Uniforms / SSBO -------------------------------------------------------------

// Input -----------------------------------------------------------------------

// Output ----------------------------------------------------------------------
layout(location = FRAG_COLOR) out vec4 outColor;

// Functions -------------------------------------------------------------------
void main()
{
    outColor = vec4(1, 1, 1, 1);
}
