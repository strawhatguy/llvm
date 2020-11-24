
#include "stdio.h"

extern int sum(int a, int b);

int main () {
  printf("Got: %d\n", sum(2, 42));
  return 0;
}
