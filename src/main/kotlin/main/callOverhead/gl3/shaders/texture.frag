#version 330

// Attributes
#define FRAG_COLOR  0

layout (location = FRAG_COLOR) out vec4 outputColor;

uniform sampler2DRect texture0;

void main()
{
    outputColor = texture(texture0, gl_FragCoord.xy);
}
