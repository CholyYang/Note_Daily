package leetcode;

class MouseCatGame {
  int n;
  public int catMouseGame(int[][] graph) {
    n = graph.length;
    //所有状态都标记为0（表示平局）
    int[][][] dp = new int[2 * n][n][n];
    int ans = helper(graph , 0 , 1 , 2 , dp);
    // 3是平局
    return ans == 3 ? 0 : ans;
  }
  public int helper(int[][] graph , int t , int x , int y , int[][][] dp){
    if(t == 2 * n) return 3;
    // 🐱赢
    if(x == y){
      dp[t][x][y] = 2;
      return 2;
    }
    // 🐭赢
    if(x == 0){
      dp[t][x][y] = 1;
      return 1;
    }
    if(dp[t][x][y] != 0){
      return dp[t][x][y];
    }
    //🐭走
    if(t % 2 == 0){
      boolean catWin = true;
      for(int i = 0 ; i < graph[x].length ; i++){
        int nx = graph[x][i];
        // 🐭的下一步
        int next = helper(graph, t + 1, nx, y, dp);
        // 如果是1 则🐭赢
        if (next == 1){
          dp[t][x][y] = 1;
          return 1;
        }
        // 只要返回的不是2 就不是🐱赢
        else if(next != 2){
          catWin = false;
        }
      }
      // 如果是🐱赢  返回2
      if(catWin){
        dp[t][x][y] = 2;
        return 2;
      }
      // 否则返回3 也就是代表平局的形式
      else{
        dp[t][x][y] = 3;
        return 3;
      }
    }
    // 🐱走
    else{
      // 先默认🐭赢
      boolean mouseWin = true;
      for (int i = 0 ; i < graph[y].length ; i++){
        int ny = graph[y][i];
        if (ny == 0) continue;
        // 🐱的下一步
        int next = helper(graph, t + 1, x, ny, dp);
        //如果返回是 2 则🐱赢
        if (next == 2){
          dp[t][x][y] = 2;
          return 2;
        }
        //返回不是 1 就不是🐭赢
        else if (next != 1) {
          mouseWin = false;
        }
      }
      if (mouseWin) {
        dp[t][x][y] = 1;
        return 1;
      }else{
        dp[t][x][y] = 3;
        return 3;
      }
    }
  }
}