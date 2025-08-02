package org.bri.usercenter.algo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.bri.usercenter.utils.EditDistance.minDistance;

/**
 * 相似匹配算法测试
 */
public class AlgoTest {


    @Test
    public void test1() {
        List<String> targetList1 = List.of("java", "c", "大二");
        List<String> targetList2 = List.of("java", "c++", "大一");
        List<String> targetList3 = List.of("python", "c", "大二");

        int score1 = minDistance(targetList1, targetList2);
        int score2 = minDistance(targetList1, targetList3);
        int score3 = minDistance(targetList2, targetList3);

        System.out.println(score1); // 2
        System.out.println(score2); // 1
        System.out.println(score3); // 3
    }
}
