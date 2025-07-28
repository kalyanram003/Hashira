import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class Hashira {

    static class Point {
        BigInteger x;
        BigInteger y;
        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) throws Exception {
        String[] fileNames = {"testcase1.json", "testcase2.json"};

        for (String fileName : fileNames) {
            Map<String, String> fileLines = readJsonFile(fileName);

            String kStr = fileLines.get("k");
            if (kStr == null) throw new IllegalArgumentException("Missing 'k' in file: " + fileName);
            int k = Integer.parseInt(kStr.trim());

            List<Point> points = parsePoints(fileLines, k);
            BigInteger secret = lagrangeInterpolation(BigInteger.ZERO, points);
            System.out.println("Secret (constant term c) for " + fileName + ": " + secret);
        }
    }

    private static Map<String, String> readJsonFile(String fileName) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;
        String currentKey = "";

        while ((line = br.readLine()) != null) {
            line = line.trim();

            if (line.startsWith("\"n\"") || line.startsWith("\"k\"")) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String key = parts[0].replaceAll("[\" ,]", "");
                    String value = parts[1].replaceAll("[\",]", "").trim();
                    map.put(key, value);
                }
            } else if (line.matches("\"\\d+\"\\s*:\\s*\\{")) {
                currentKey = line.replaceAll("[\" {:]", "");
            } else if (line.contains("base")) {
                String base = line.split(":")[1].replaceAll("[\",]", "").trim();
                map.put("base_" + currentKey, base);
            } else if (line.contains("value")) {
                String value = line.split(":")[1].replaceAll("[\",]", "").trim();
                map.put("value_" + currentKey, value);
            }
        }

        br.close();
        return map;
    }

    private static List<Point> parsePoints(Map<String, String> map, int k) {
        List<Point> points = new ArrayList<>();

        for (String key : map.keySet()) {
            if (key.startsWith("base_")) {
                String id = key.substring(5);
                String base = map.get("base_" + id).trim();
                String value = map.get("value_" + id).trim();

                BigInteger x = new BigInteger(id.trim());
                BigInteger y = new BigInteger(value, Integer.parseInt(base));
                points.add(new Point(x, y));
            }
        }

        points.sort(Comparator.comparing(p -> p.x));
        return points.subList(0, k); 
    }

    private static BigInteger lagrangeInterpolation(BigInteger x, List<Point> points) {
        BigInteger result = BigInteger.ZERO;
        int k = points.size();

        for (int i = 0; i < k; i++) {
            BigInteger xi = points.get(i).x;
            BigInteger yi = points.get(i).y;

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    BigInteger xj = points.get(j).x;
                    numerator = numerator.multiply(x.subtract(xj));
                    denominator = denominator.multiply(xi.subtract(xj));
                }
            }

            BigInteger term = yi.multiply(numerator).divide(denominator);
            result = result.add(term);
        }

        return result;
    }
}
