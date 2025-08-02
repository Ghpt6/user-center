package org.bri.usercenter.utils;

import java.util.List;

public class EditDistance {
    /**
     * 编辑距离算法，将一个字符串转换为另一个字符串所需的最少编辑操作次数
     * 结果越小，说明两个字符串越相似
     * @param targetList1
     * @param targetList2
     * @return
     */
    public static int minDistance(List<String> targetList1, List<String> targetList2) {
        int n = targetList1.size();
        int m = targetList2.size();

        if (n * m == 0) {
            return n + m;
        }

        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            d[i][0] = i;
        }

        for (int j = 0; j < m + 1; j++) {
            d[0][j] = j;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                int left = d[i - 1][j] + 1;
                int down = d[i][j - 1] + 1;
                int left_down = d[i - 1][j - 1];
                if (!targetList1.get(i - 1).equals(targetList2.get(j - 1))) {
                    left_down += 1;
                }
                d[i][j] = Math.min(left, Math.min(down, left_down));
            }
        }

        return d[n][m];
    }
}
