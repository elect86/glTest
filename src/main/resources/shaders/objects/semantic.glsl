
// Attributes
#define POSITION    0
#define COLOR       3
#define DRAW_ID     5

// Uniform
#define TRANSFORM0  1
#define TRANSFORM1  2

// Storage
#define CONSTANT    1

// Interface
#define BLOCK   0

// Output
#define FRAG_COLOR  0


precision highp float;
precision highp int;

layout(std140, column_major) uniform;
layout(std430, column_major) buffer;