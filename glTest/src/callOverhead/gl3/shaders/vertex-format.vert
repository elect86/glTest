#version 450

// Attributes
#define POSITION    0

layout (location = 0) in vec3 Position0;
layout (location = 1) in vec3 Position1;

uniform mat4 modelToClip;

void main()
{
    gl_Position = modelToClip * vec4((gl_VertexID % 2) == 0? Position0 : Position1, 1.0);
}
