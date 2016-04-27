#version 330

// Attributes
#define FRAG_COLOR  0

layout (location = FRAG_COLOR) out vec4 outputColor;

void main()
{
    outputColor = vec4(1, 0.5, 0, 1);
}
