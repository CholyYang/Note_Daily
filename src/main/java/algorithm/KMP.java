package algorithm;

/**
 * @author Choly
 * @version 2020-08-26
 *
 * 其中 X 为影子状态，未匹配上时，最优的回溯位置
 */
public class KMP {
    private int[][] dp; // 状态数组 dp[状态][字符] = 下一个状态
    private String mode;   // 模式串

    public KMP(String mode) {
        this.mode = mode;
        int M = mode.length();
        dp = new int[M][256];
        dp[0][mode.charAt(0)] = 1;
        int X = 0; // 影子状态

        for(int j = 1; j < M; j++) {
            for(int k = 0; k < 256; k++) {
                dp[j][k] = dp[X][k];
            }
            dp[j][mode.charAt(j)] = j + 1;
            X = dp[X][mode.charAt(j)];
        }
    }

    public int search(String compareStr) {
        int M = mode.length();
        int N = compareStr.length();

        int next = 0;
        for(int i = 0; i < N; i++) {
            next = dp[next][compareStr.charAt(i)];
            if (next == M) return N - M + 1;
        }
        return -1;
    }
}
