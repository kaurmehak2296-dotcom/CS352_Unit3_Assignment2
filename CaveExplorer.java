import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

public class CaveExplorer {
    private char[][] cave;
    private char[][] finalLayout;
    private String path = "";

    // Coordinates of one position in the cave.
    private static class Position {
        private final int row;
        private final int col;

        private Position(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    // Original Unit 3 cave.
    public CaveExplorer() {
        cave = new char[][] {
            {'R', 'R', 'R', 'R', 'R', 'R'},
            {'R', '.', '.', 'S', 'R', 'R'},
            {'R', '.', 'R', 'R', 'R', 'R'},
            {'R', '.', 'M', 'R', 'R', 'R'},
            {'R', 'R', 'R', 'R', 'R', 'R'}
        };

        resetLayout();
    }

    // Read a cave from a text file.
    public CaveExplorer(String fname) throws FileNotFoundException {
        try (Scanner in = new Scanner(new File(fname))) {
            int rows = in.nextInt();
            int cols = in.nextInt();
            in.nextLine();

            if (rows <= 0 || cols <= 0) {
                throw new IllegalArgumentException(
                    "Cave dimensions must be positive.");
            }

            cave = new char[rows][cols];

            for (int row = 0; row < rows; row++) {
                String line = in.nextLine();

                if (line.length() != cols) {
                    throw new IllegalArgumentException(
                        "Incorrect width in cave row " + row);
                }

                for (int col = 0; col < cols; col++) {
                    cave[row][col] = line.charAt(col);
                }
            }
        }

        resetLayout();
    }

    // Keep the original cave unchanged so solve can run again.
    private void resetLayout() {
        finalLayout = new char[cave.length][];

        for (int row = 0; row < cave.length; row++) {
            finalLayout[row] = cave[row].clone();
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for (int row = 0; row < finalLayout.length; row++) {
            for (int col = 0; col < finalLayout[row].length; col++) {
                result.append(finalLayout[row][col]);
            }
            result.append('\n');
        }

        return result.toString();
    }

    public boolean solve() {
        path = "";
        resetLayout();

        Position start = null;

        // Find S.
        for (int row = 0; row < cave.length; row++) {
            for (int col = 0; col < cave[row].length; col++) {
                if (cave[row][col] == 'S') {
                    start = new Position(row, col);
                }
            }
        }

        if (start == null) {
            return false;
        }

        boolean[][] visited =
            new boolean[cave.length][cave[0].length];

        // ArrayDeque is used as a stack.
        Deque<Position> stack = new ArrayDeque<>();
        StringBuilder currentPath = new StringBuilder();

        stack.push(start);
        visited[start.row][start.col] = true;

        // Try north, east, south, west.
        int[] rowChange = {-1, 0, 1, 0};
        int[] colChange = {0, 1, 0, -1};
        char[] direction = {'n', 'e', 's', 'w'};

        while (!stack.isEmpty()) {
            Position current = stack.peek();

            // The stack now contains only the successful route.
            if (cave[current.row][current.col] == 'M') {
                path = currentPath.toString();

                // Mark the successful path, preserving S and M.
                for (Position position : stack) {
                    if (cave[position.row][position.col] == '.') {
                        finalLayout[position.row][position.col] = '+';
                    }
                }

                return true;
            }

            boolean moved = false;

            for (int i = 0; i < direction.length; i++) {
                int nextRow = current.row + rowChange[i];
                int nextCol = current.col + colChange[i];

                if (nextRow < 0 || nextRow >= cave.length
                        || nextCol < 0
                        || nextCol >= cave[nextRow].length) {
                    continue;
                }

                char next = cave[nextRow][nextCol];

                if (!visited[nextRow][nextCol]
                        && (next == '.' || next == 'M')) {
                    visited[nextRow][nextCol] = true;
                    stack.push(new Position(nextRow, nextCol));
                    currentPath.append(direction[i]);
                    moved = true;
                    break;
                }
            }

            // Dead end: return to the previous position.
            if (!moved) {
                stack.pop();

                // Remove the move that entered this dead end.
                if (currentPath.length() > 0) {
                    currentPath.deleteCharAt(
                        currentPath.length() - 1);
                }
            }
        }

        // Every reachable position was explored without finding M.
        return false;
    }

    public String getPath() {
        return path;
    }

    private static void runTest(String label, CaveExplorer explorer) {
        System.out.println(label);
        System.out.println("Starting layout:");
        System.out.print(explorer);

        boolean solved = explorer.solve();

        System.out.println("Final layout:");
        System.out.print(explorer);
        System.out.println("Pool found: " + solved);

        if (solved) {
            System.out.println("Path: " + explorer.getPath());
        }
        else {
            System.out.println("No path to the mirror pool.");
        }

        System.out.println();
    }

    public static void main(String[] args) {
        runTest("CAVE 1: Simple path", new CaveExplorer());

        String[] filenames = {
            "branching.txt",
            "multipleBranches.txt",
            "noPath.txt"
        };

        for (int i = 0; i < filenames.length; i++) {
            try {
                CaveExplorer explorer =
                    new CaveExplorer(filenames[i]);

                runTest("CAVE " + (i + 2) + ": " + filenames[i],
                        explorer);
            }
            catch (FileNotFoundException e) {
                System.out.println(
                    "Cannot find " + filenames[i]);
            }
        }
    }
}