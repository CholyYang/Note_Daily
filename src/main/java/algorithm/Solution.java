package algorithm;

/**
 * @author Choly
 * @version 1/2/21
 */
public class Solution {
    public int[] maxSlidingWindow(int[] nums, int k) {
        if (k == 1) return nums;
        int[] retArr = new int[nums.length - k + 1];
        int tailMax = nums[0];
        for (int i = 0; i <= nums.length - k; i++) {
            if (nums[i] == tailMax) {
                int currentMax = nums[i + 1];
                for (int j = i + 1; j <= i + k - 1; j++) {
                    if (nums[j] > currentMax) {
                        currentMax = nums[j];
                    }
                }
                tailMax = currentMax;
            } else {
                if (nums[i + k - 1] > tailMax) tailMax = nums[i + k - 1];
            }
            if (nums[i] < tailMax) retArr[i] = tailMax;
            else retArr[i] = nums[i];
        }
        return retArr;
    }

    public static void main(String[] args) {
        int[] arr = {-7,-8,7,5,7,1,6,0};

        int[] arr2 = new Solution().maxSlidingWindow(arr, 4);
        System.out.println(arr2);
    }
}
