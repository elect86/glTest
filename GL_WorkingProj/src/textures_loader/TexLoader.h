#pragma once

#include <windows.h>
#include "../math/mathlib.h"

#define del_it(a) if (a) { delete a; a = NULL; }
#define del_array(a) if (a) { delete []a; a = NULL; }
const int MAX_TEX_NAME_LEN = 256;

class TextureLoader
{
public:
	TextureLoader() : data(NULL) {}
	~TextureLoader()
	{
		del_it(data);
	}
	void clear();
	bool load_bmp(const char *name);

	char tex_name[MAX_TEX_NAME_LEN];
	BYTE *data;
	int width;
	int height;
	int num_mipmaps;
	int Bpp;
	//DXGI_FORMAT format;

	int data_pitch;
	int data_size;
};