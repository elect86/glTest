//https://www.gamedev.net/topic/438752-idenitfying-cpu-brand--model-c/
//b2b3

#include <string>
#include <windows.h>
#include "Processor.h"

using namespace std;

Processor::Processor(void) :
	has_mmx(false),
	has_mmx_ext(false),
	has_3dnow(false),
	has_3dnow_ext(false),
	has_sse(false),
	has_sse2(false),
	has_sse3(false),
	is_htt(false),
	stepping(0),
	model(0),
	family(0),
	type(0),
	ext_model(0),
	ext_family(0),
	cpu_name("(unknown)"),
	cpu_vendor("(unknown)"),
	cpu_speed(0)
{
	MeasureSpeed();
	DoCPUID();
}

void Processor::MeasureSpeed(void)
{
	LARGE_INTEGER    s, e, freq;
	unsigned __int64 cs, ce;

	// Determine timer frequency.
	QueryPerformanceFrequency(&freq);

	QueryPerformanceCounter(&s);
	cs = GetTSC();

	// Wait for a while...
	for (volatile long i = 0; i < 1000000; ++i);

	ce = GetTSC();
	QueryPerformanceCounter(&e);

	// Calculate frequency.
	cpu_speed = (unsigned long)((ce - cs) * freq.QuadPart / (e.QuadPart - s.QuadPart));
}

// this is the "core" method - it will cal CPUID instruction and parse output
void Processor::DoCPUID(void)
{
	char cpu_name_string[49] = { 0 };  // max 48 chars + terminating 0
	char cpu_vendor_id_string[13] = { 0 }; // max 12 chars + terminating 0
	unsigned int cpu_feat_eax = 0;
	unsigned int cpu_feat_edx = 0;
	unsigned int cpu_feat_ecx = 0;
	unsigned int cpu_feat_ext_edx = 0;

	__asm
	{
		mov     eax, 0x00000000              // first CPUID function, always supported (on reasonable cpu)
		cpuid                                // get info
		mov     DWORD PTR[cpu_vendor_id_string + 0], ebx  // copy vendor id string
		mov     DWORD PTR[cpu_vendor_id_string + 4], edx
		mov     DWORD PTR[cpu_vendor_id_string + 8], ecx
		test    eax, eax
		jz      no_features                  // if eax is 0, no info will be available

		mov     eax, 0x00000001              // get extended info about cpu
		cpuid
		mov[cpu_feat_eax], eax          // store data for later processing
		mov[cpu_feat_edx], edx
		mov[cpu_feat_ecx], ecx

		mov     eax, 0x80000000              // first extended function
		cpuid

		// now test which extended functions are supported
		cmp     eax, 0x80000001              // is eax < 0x80000001
		jb      no_features                  // yes -> jump to no_features label
		cmp     eax, 0x80000004              // is eax < 0x80000004
		jb      ext_feats_only               // yes -> jump to ext_feats_only label

												// now get name of the cpu
												mov     eax, 0x80000002
												cpuid
												mov     DWORD PTR[cpu_name_string + 0], eax
												mov     DWORD PTR[cpu_name_string + 4], ebx
												mov     DWORD PTR[cpu_name_string + 8], ecx
												mov     DWORD PTR[cpu_name_string + 12], edx

												mov     eax, 0x80000003
												cpuid
												mov     DWORD PTR[cpu_name_string + 16], eax
												mov     DWORD PTR[cpu_name_string + 20], ebx
												mov     DWORD PTR[cpu_name_string + 24], ecx
												mov     DWORD PTR[cpu_name_string + 28], edx

												mov     eax, 0x80000004
												cpuid
												mov     DWORD PTR[cpu_name_string + 32], eax
												mov     DWORD PTR[cpu_name_string + 36], ebx
												mov     DWORD PTR[cpu_name_string + 40], ecx
												mov     DWORD PTR[cpu_name_string + 44], edx

												ext_feats_only :
		// get extended features
		mov     eax, 0x80000001
			cpuid
			mov[cpu_feat_ext_edx], edx

			no_features :
		// done
	} // __asm

		// now process data we got from cpu
	cpu_name = string(cpu_name_string);
	cpu_vendor = string(cpu_vendor_id_string);

	stepping = cpu_feat_eax & 0xF;
	model = (cpu_feat_eax >> 4) & 0xF;
	family = (cpu_feat_eax >> 8) & 0xF;
	type = (cpu_feat_eax >> 12) & 0x3;
	ext_model = (cpu_feat_eax >> 16) & 0xF;
	ext_family = (cpu_feat_eax >> 20) & 0xFF;

	has_mmx = (cpu_feat_edx >> 23) & 0x1;
	has_sse = (cpu_feat_edx >> 25) & 0x1;
	has_sse2 = (cpu_feat_edx >> 26) & 0x1;
	is_htt = (cpu_feat_edx >> 28) & 0x1;

	has_sse3 = cpu_feat_ecx & 0x1;

	has_mmx_ext = (cpu_feat_ext_edx >> 22) & 0x1;
	has_3dnow = (cpu_feat_ext_edx >> 31) & 0x1;
	has_3dnow_ext = (cpu_feat_ext_edx >> 30) & 0x1;
}
