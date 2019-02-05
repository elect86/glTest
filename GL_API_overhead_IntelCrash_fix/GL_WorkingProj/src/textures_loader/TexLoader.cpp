#include "TexLoader.h"
#include <stdio.h>


void TextureLoader::clear()
{
	del_it(data);
}

bool TextureLoader::load_bmp(const char *name)
{
	clear();

	FILE* bmp_file;
	BITMAPFILEHEADER bmfileheader;
	BITMAPINFOHEADER bminfoheader;

	int i;
	fopen_s(&bmp_file, name, "rb");

	if (!bmp_file) return NULL;

	fread(&bmfileheader, sizeof(BITMAPFILEHEADER), 1, bmp_file);
	fread(&bminfoheader, sizeof(BITMAPINFOHEADER), 1, bmp_file);

	width = bminfoheader.biWidth;
	height = bminfoheader.biHeight;

	// Check the type field to make sure we have a .bmp file
	if (memcmp(&bmfileheader.bfType, "BM", 2))
	{
		fclose(bmp_file);
		return NULL;
	}

	int depth = 1;
	Bpp = bminfoheader.biBitCount;


	int components = 0;
	if (Bpp == 32) { components = 4; }
	else
	if (Bpp == 24) { components = 3; }
	else
	if (Bpp == 8) { components = 1; }
	else
	{
		int tmp = Bpp;
		fprintf(stderr, "unknown image format %s Bpp=%i\n", name, tmp);
		fclose(bmp_file);
		return NULL;
	}

	BYTE *src_data = new BYTE[width*height*components];
	memset(src_data, 0, width*height*components);
	data = src_data;

	// *3* IF BPP AREN'T 24, LOAD PALETTE.
	//BYTE *Palette;
	if (Bpp != 24 && Bpp != 32)
	{
		BYTE *Palette = new BYTE[(1 << Bpp) * 4];
		fread(Palette, sizeof(BYTE)*(1 << Bpp) * 4, 1, bmp_file);
		del_it(Palette);
	}

	// Jump to the location where the pixel data is stored
	fseek(bmp_file, bmfileheader.bfOffBits, SEEK_SET);


	if (bminfoheader.biCompression == BI_RLE4)
		fprintf(stderr, "file %s with Compression BI_RLE4\n", name);
	if (bminfoheader.biCompression == BI_RLE8)
		fprintf(stderr, "file %s with Compression BI_RLE8\n", name);

	fread(src_data, sizeof(BYTE)*width*height*components, 1, bmp_file);


	//flip vertical
	BYTE tmp;
	for (int j = 0; j<(height / 2); j++)
	{
		for (int i = 0; i<width; i++)
		{
			for (int k = 0; k<components; k++)
			{
				tmp = data[(i + j*width)*components + k];
				data[(i + j*width)*components + k] = data[(i + (height - 1 - j)*width)*components + k];
				data[(i + (height - 1 - j)*width)*components + k] = tmp;
			}
		}
	}

	if ((Bpp == 32) || (Bpp == 24))
	{
		BYTE tmp;
		//swap red and blue
		for (i = 0; i<width*height; i++)
		{
			tmp = data[i*components + 0];
			data[i*components + 0] = data[i*components + 2];
			data[i*components + 2] = tmp;
		}
	}

	if (Bpp == 24)
	{
		data = new BYTE[width*height*4];
		//reorganize data
		for (i = 0; i<width*height; i++)
		{
			data[i * 4 + 0] = src_data[i * 3 + 0];
			data[i * 4 + 1] = src_data[i * 3 + 1];
			data[i * 4 + 2] = src_data[i * 3 + 2];
			data[i * 4 + 3] = 0;
		}
		components = 4;
	}

	fclose(bmp_file);


	sprintf_s(&tex_name[0], MAX_TEX_NAME_LEN, "%s", name);
	num_mipmaps = 1;

	data_pitch = components * width;
	data_size = components * width * height;

	return true;
}