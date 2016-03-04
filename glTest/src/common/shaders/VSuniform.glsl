#version 450

layout (location = 0) in vec3 Position;

uniform mat4 modelToClip;

uniform int u;

void main()
{
    gl_Position = modelToClip * vec4(Position, u);
}
