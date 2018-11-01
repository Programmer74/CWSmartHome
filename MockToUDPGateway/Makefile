SRC_DIR := ./src
INC_DIR += ./inc
OBJ_DIR := ./obj
SRC_FILES := $(wildcard $(SRC_DIR)/*.cpp)
OBJ_FILES := $(patsubst $(SRC_DIR)/%.cpp,$(OBJ_DIR)/%.o,$(SRC_FILES))

EXEC := cppudpmock

all: $(OBJ_FILES)
	$(CXX) $(LDFLAGS) $^ $(LIBS) -o $(EXEC)

$(OBJ_DIR)/%.o: $(SRC_DIR)/%.cpp objdir
	$(CXX) $(CFLAGS) -I$(INC_DIR) -c $< $(LIBS) -o $@

objdir:
	mkdir -p $(OBJ_DIR)

clean:
	rm -rf $(OBJ_DIR) $(EXEC)
