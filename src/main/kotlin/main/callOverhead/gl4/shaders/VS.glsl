#version 450

layout (location = 0) in vec3 Position;

uniform mat4 modelToClip;

void main()
{
    gl_Position = modelToClip * vec4(Position, 1.0);
}
