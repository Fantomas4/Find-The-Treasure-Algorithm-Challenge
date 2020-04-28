import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Mines {

    List<int[]> minesPos = new ArrayList<>(); // Contains the mines position coordinates read from the input file.
    int[] startPos = new int[2]; // Contains the start position coordinates read from the input file.
    int[] treasurePos = new int[2]; // Contains the treasure position coordinates read from the input file.

    Set<int[]> hull = new HashSet<>(); // Contains the points that define the Convex Hull.
    List<int[]> topSubHull = new ArrayList<>(); // Contains the points that define the top sub-hull of the Convex Hull.
    List<int[]> bottomSubHull = new ArrayList<>(); // Contains the points that define the bottom sub-hull of the Convex Hull.

    Mines(String inputFileDir) {
        // Check if loadFile() successfully read the specified input file.
        // If so, call findShortestPath().
        if (loadFile(inputFileDir)) {
            findShortestPath();
        }
    }

    /**
     * Method used to read the contents (point coordinates) of the specified
     * input file and store them into minePos.
     * @param dir Input file directory.
     * @return True if the specified input file was successfully loaded, False if an error occurred.
     */
    private boolean loadFile(String dir) {
        File file = new File(dir);
        Scanner scanner;
        String line;
        String[] coordinates;

        try {
            scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();

                if (!line.equals("")) {
                    coordinates = line.split(" ");

                    int[] point = new int[2];
                    point[0] = Integer.parseInt(coordinates[0]);
                    point[1] = Integer.parseInt(coordinates[1]);

                    minesPos.add(point);
                }
            }
            this.startPos = minesPos.remove(0); // Remove the first element from minePos and set its value to startPos.
            this.treasurePos = minesPos.remove(0); // Remove the second element (now in the first element position, since
                                                     // we extracted the first element for startPos) from minePos and set
                                                     // its value to treasurePos.
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("*** Error: File not Found. ***");
            return false;
        }
    }

    /**
     * Calculates the distance between 2 given points.
     * @param point1 The first point.
     * @param point2 The second point.
     * @return The distance between the 2 given points.
     */
    private double pointsDistance(int[] point1, int[] point2) {
        return Math.sqrt(Math.pow(point2[0] - point1[0], 2) + Math.pow(point2[1] - point1[1], 2));
    }

    /**
     * Used to find the max point using the maximum angle.
     * @param p1 Point p1 of the given p1pn line.
     * @param pn Point pn of the given p1pn line.
     * @param side The side on which the max point should be on.
     * @return The max point index in minesPos list.
     */
    public int maxPointUsingAngle(int[] p1, int[] pn, int side) {
        double p1pnDistance = pointsDistance(p1, pn);

        int maxIndex = -1;
        double maxAngle = -1;

        for (int i = 0; i < minesPos.size(); i++) {
            int[] tp = minesPos.get(i);

            if (determinePointSide(tp, p1, pn) == side) {
                double tppnDistance = pointsDistance(tp, pn);
                double tpp1Distance = pointsDistance(tp, p1);
                double angle = Math.acos((Math.pow(tppnDistance, 2) + Math.pow(tpp1Distance, 2) - Math.pow(p1pnDistance, 2))
                        / (2 * tppnDistance * tpp1Distance));

                if (angle > maxAngle) {
                    maxIndex = i;
                    maxAngle = angle;
                }
            }
        }
        return maxIndex;
    }

    //    ************ ATTENTION : slide 197, book pdf
    private int determinePointSide(int[] targetPoint, int[] linePoint1, int[] linePoint2) {
        int value = (targetPoint[1] - linePoint1[1]) * (linePoint2[0] - linePoint1[0]) -
                (linePoint2[1] - linePoint1[1]) * (targetPoint[0] - linePoint1[0]);

        if (value > 0) {
            return 1;
        } else if (value < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    //    ************ ATTENTION : slide 197, book pdf
    private int pointToLineDist(int[] targetPoint, int[] linePoint1, int[] linePoint2) {
        return Math.abs((targetPoint[1] - linePoint1[1]) * (linePoint2[0] - linePoint1[0]) -
                (linePoint2[1] - linePoint1[1]) * (targetPoint[0] - linePoint1[0]));
    }

    private void quickhull(int[] p1, int[] pn, int side) {
        int maxIndex = -1;
        int maxDistance = 0;

        //Find the point with the maximum distance from line p1pn
        for (int i = 0; i < minesPos.size(); i++) {
            int tempDistance = pointToLineDist(minesPos.get(i), p1, pn);
            if (determinePointSide(minesPos.get(i), p1, pn) == side) {
                if (tempDistance > maxDistance) {
//                If the current mine point has a greater distance from p1pn than maxDistance
                    maxIndex = i;
                    maxDistance = tempDistance;
                } else if (tempDistance == maxDistance) {
                    // Find the index of the maximum point based on the angle maximization technique.
                    maxIndex = maxPointUsingAngle(p1, pn, side);

                    // The maximum point was found based on the maximum angle, so we can now break the loop.
                    break;
                    }
                }
            }


        if (maxIndex == -1) {
            //If no points are found, add p1 and pn to the
            // appropriate Sub-Hull arraylist
//            if (side == 1) {
//                topSubHull.add(p1);
//                topSubHull.add(pn);
//            } else if (side == -1) {
//                bottomSubHull.add(p1);
//                bottomSubHull.add(pn);
//            }
            hull.add(p1);
            hull.add(pn);
            return;
        }

        // Na afairw apo to minesPos osa stoixeia elegxw oste na meiwsw ton arithmo epanalipseon
        // stiw epomenes anazitiseis quickhull?

        quickhull(minesPos.get(maxIndex), p1, -determinePointSide(pn, minesPos.get(maxIndex), p1));
        quickhull(minesPos.get(maxIndex), pn, -determinePointSide(p1, minesPos.get(maxIndex), pn));
    }

    private void findConvexHull() {
        quickhull(startPos, treasurePos, 1);
        quickhull(startPos, treasurePos, -1);

//        //        DIAG !!!
//        System.out.println("Convex hull is: ");
//        for (int[] point : hull) {
//            System.out.printf("%d %d\n", point[0], point[1]);
//        }
    }

    private void findSubHulls() {
        for (int[] point : hull) {
            int side = determinePointSide(point, startPos, treasurePos);
            if (side == 1) {
                topSubHull.add(point);
            } else if (side == -1) {
                bottomSubHull.add(point);
            }
        }
    }

    private List<int[]> merge(List<int[]> subList1, List<int[]> subList2) {
        int i = 0;
        int j = 0;

        List<int[]> mergedList = new ArrayList<>();

        int size1 = subList1.size();
        int size2 = subList2.size();
        while (i < size1 && j < size2) {
            // Compare points based on their x value
            if (subList1.get(i)[0] <= subList2.get(j)[0]) {
                mergedList.add(subList1.get(i));
                i += 1;
            } else {
                mergedList.add(subList2.get(j));
                j += 1;
            }
        }

        if (i == size1) {
            while (j < size2) {
                mergedList.add(subList2.get(j));
                j += 1;
            }
        } else {
            while (i < size1) {
                mergedList.add(subList1.get(i));
                i += 1;
            }
        }

        return mergedList;
    }

    private List<int[]> mergeSort(List<int[]> initialList) {
        double size = initialList.size();

        if (size > 1) {
            List<int[]> subList1 = new ArrayList<>();
            for (int i = 0; i < Math.floor(size / 2); i++) {
                subList1.add(initialList.get(i));
            }

            List<int[]> subList2 = new ArrayList<>();
            for (int i = (int) Math.floor(size / 2); i < size; i++) {
                subList2.add(initialList.get(i));
            }

            subList1 = mergeSort(subList1);
            subList2 = mergeSort(subList2);
            initialList = merge(subList1, subList2);
        }

        return initialList;
    }

    private double calculatePathDistance(List<int[]> pathPoints) {
        double totalDistance = 0;

        for (int i = 0; i < pathPoints.size() - 1; i++) {
            totalDistance += pointsDistance(pathPoints.get(i), pathPoints.get(i + 1));
        }

        return totalDistance;
    }

    private String pathPointsString(List<int[]> pathPoints) {
        StringBuilder stringBuilder = new StringBuilder();

        int size = pathPoints.size();
        for (int i = 0; i < size - 1; i++) {
            stringBuilder.append(String.format("(%d,%d)-->", pathPoints.get(i)[0], pathPoints.get(i)[1]));
        }
        stringBuilder.append(String.format("(%d,%d)", pathPoints.get(size - 1)[0], pathPoints.get(size - 1)[1]));

        return stringBuilder.toString();
    }

    public void findShortestPath() {
        findConvexHull();
        findSubHulls();


        List<int[]> topSortedPath = mergeSort(topSubHull);
        // Add start position to the start of the sorted top path list
        topSortedPath.add(0, startPos);
        // Add treasure position to the end of the sorted top path list
        topSortedPath.add(treasurePos);
//        //        DIAG !!!
//        System.out.println("===================");
//        for (int[] point : topSortedPath) {
//            System.out.printf("%d %d\n", point[0], point[1]);
//        }

        List<int[]> bottomSortedPath = mergeSort(bottomSubHull);
        // Add start position at the start of the sorted bottom path list
        bottomSortedPath.add(0, startPos);
        // Add treasure position at the end of the sorted bottom path list
        bottomSortedPath.add(treasurePos);
//        //        DIAG !!!
//        System.out.println("===================");
//        for (int[] point : bottomSortedPath) {
//            System.out.printf("%d %d\n", point[0], point[1]);
//        }

        double topPathDistance = calculatePathDistance(topSortedPath);
        double bottomPathDistance = calculatePathDistance(bottomSortedPath);

        if (topPathDistance <= bottomPathDistance) {
            System.out.printf("The shortest distance is %.5f\n", topPathDistance);
            System.out.println("The shortest path is:" + pathPointsString(topSortedPath));
        } else {
            System.out.printf("The shortest distance is %.5f\n", bottomPathDistance);
            System.out.println("The shortest path is:" + pathPointsString(bottomSortedPath));
        }


    }

    public static void main(String[] args) {
        Mines mines = new Mines(args[0]);

    }
}
