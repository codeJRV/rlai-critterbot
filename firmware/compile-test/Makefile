# A primitive Makefile until I have a better grasp of how things work
#
# It assumes that the object files are in the current directory
#  You can get object files by compiling the ../libs directory
# It will also compile the startup assembly file for you.

THUMB=-marm

# Directories
GCC_LIB_DIR=/opt/local/lib/gcc/arm-elf/4.1.1

GCC_LIBS=$(GCC_LIB_DIR)/libgcc.a
INCLUDES=-Iinclude -I.

# Flags
# Uncomment to generate memory map
# Rename to -Wl,-M if arm-elf-gcc is used to link
# PRODUCE_MEMORY_MAP=-M
PRODUCE_MEMORY_MAP=

all: armcode.bin
clean:
	rm *.o
	rm -f armcode.elf armcode.hex armcode.bin
	
# Object translation
armcode.bin: armcode.elf
	arm-elf-objcopy -O binary armcode.elf armcode.bin

# Object linking
# Can alternatively be done using arm-elf-gcc, which provides crt0 and such
armcode.elf: startup_SAM7S.o jlink_test.o
	arm-elf-ld -Tarmcode.ld --oformat elf32-littlearm *.o $(GCC_LIBS) --output armcode.elf $(PRODUCE_MEMORY_MAP)

# Startup code
startup_SAM7S.o: startup_SAM7S.S
	arm-elf-gcc $(THUMB) $(INCLUDES) -mcpu=arm7tdmi -c -Wall -x assembler-with-cpp startup_SAM7S.S

Cstartup.o: Cstartup.S
	arm-elf-gcc $(THUMB) $(INCLUDES) -mcpu=arm7tdmi -c -Wall -x assembler-with-cpp Cstartup.S

# Simple program (from assignment 1)
serialecho.o: serialecho.c
	arm-elf-gcc $(THUMB) $(INCLUDES) -mcpu=arm7tdmi -c -Wall serialecho.c

jlink_test.o: serialecho.c
	arm-elf-gcc $(THUMB) $(INCLUDES) -mcpu=arm7tdmi -c -Wall jlink_test.c

Cstartup_SAM7.o: Cstartup_SAM7.c
	arm-elf-gcc $(THUMB) $(INCLUDES) -mcpu=arm7tdmi -c -Wall Cstartup_SAM7.c
	
	
