#include <vector>
#include <array>
#include <iostream>

class person
{
public:
	char *name;
	int age = 5;
};

void arr()
{
	std::array<person *, 10> arr;
	for (int i = 0; i < arr.size() / 2; i++)
	{
		arr[i] = new person;
	}
	for (person *p : arr)
	{
		std::cout << (p == NULL) << "\n";
	}
}

void vect()
{
	std::vector<person *> arr(10);
	for (int i = 0; i < arr.size() / 2; i++)
	{
		arr[i] = new person;
	}
	for (person *p : arr)
	{
		std::cout << (p == NULL) << "\n";
	}
}

void vect2()
{
	std::vector<std::vector<person *> *> *arr;
	//arr = new std::vector<std::vector<person *> *>(10, new std::vector<person *>(5));
	arr = new std::vector<std::vector<person *> *>(10, nullptr);
	/*for (int i = 0; i < arr.size() / 2; i++)
	{
		arr[i] = new person;
	}*/
	for (std::vector<person *> *p : *arr)
	{
		//std::cout << (p->size()) << "\n";
		std::cout << (p == NULL) << "\n";
	}
}

int main()
{
	//vect();
	vect2();

	return 0;
}
