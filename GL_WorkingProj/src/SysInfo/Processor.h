//https://www.gamedev.net/topic/438752-idenitfying-cpu-brand--model-c/
//b2b3

#ifndef PROCESSOR_INCLUDED
#define PROCESSOR_INCLUDED

#include <string>

class Processor
{
public:
	Processor(void);

	bool HasSSE(void) const
	{
		return has_sse;
	}

	bool HasSSE2(void) const
	{
		return has_sse2;
	}

	bool HasSSE3(void) const
	{
		return has_sse3;
	}

	bool HasMMX(void) const
	{
		return has_mmx;
	}

	bool HasMMXExt(void) const
	{
		return has_mmx_ext;
	}

	bool Has3DNow(void) const
	{
		return has_3dnow;
	}

	bool Has3DNowExt(void) const
	{
		return has_3dnow_ext;
	}

	bool IsHTT(void) const
	{
		return is_htt;
	}

	std::string GetName(void) const
	{
		return cpu_name;
	}

	std::string GetVendorID(void) const
	{
		return cpu_vendor;
	}

	std::string GetVendorName(void) const
	{
		if (cpu_vendor == "AuthenticAMD")
		{
			return std::string("AMD");
		}
		else if (cpu_vendor == "GenuineIntel")
		{
			return std::string("Intel");
		}
		else if (cpu_vendor == "CyrixInstead")
		{
			return std::string("Cyrix");
		}
		else if (cpu_vendor == "CentaurHauls")
		{
			return std::string("Centaur");
		}
		else if (cpu_vendor == "RiseRiseRise")
		{
			return std::string("Rise");
		}
		else if (cpu_vendor == "GenuineTMx86")
		{
			return std::string("Transmeta");
		}
		else if (cpu_vendor == "UMC UMC UMC ")
		{
			return std::string("UMC");
		}
		else
		{
			return cpu_vendor;
		}
	}

	unsigned long GetSpeed(void) const
	{
		return cpu_speed;
	}

	int GetStepping(void) const
	{
		return stepping;
	}

	int GetModel(void) const
	{
		return model;
	}

	int GetFamily(void) const
	{
		return family;
	}

	int GetType(void) const
	{
		return type;
	}

	int GetExtModel(void) const
	{
		return ext_model;
	}

	int GetExtFamily(void) const
	{
		return ext_family;
	}

private:

	bool has_mmx;
	bool has_mmx_ext;
	bool has_3dnow;
	bool has_3dnow_ext;
	bool has_sse;
	bool has_sse2;
	bool has_sse3;
	bool is_htt;

	int stepping;
	int model;
	int family;
	int type;
	int ext_model;
	int ext_family;

	std::string cpu_name;
	std::string cpu_vendor;

	unsigned long cpu_speed;

	unsigned __int64 GetTSC()
	{
		__asm
		{
			rdtsc
		}
	}

	void MeasureSpeed(void);

	void DoCPUID(void);
};


#endif
