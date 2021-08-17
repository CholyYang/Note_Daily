package leetcode

/**
 * @author Choly
 * @version 5/17/21
 */
//在二叉树中，根节点位于深度 0 处，每个深度为 k 的节点的子节点位于深度 k+1 处。
//
//如果二叉树的两个节点深度相同，但 父节点不同 ，则它们是一对堂兄弟节点。
//
//我们给出了具有唯一值的二叉树的根节点 root ，以及树中两个不同节点的值 x 和 y 。
//
//只有与值 x 和 y 对应的节点是堂兄弟节点时，才返回 true 。否则，返回 false。
object BtreeBrother {
  var firstPV = -1
  var firstHeight = 0
  var secondPV = -1
  var secondHeight = 0

  def findXOrY(root: TreeNode, x: Int, y: Int, depth: Int): Unit = {
    if ((root.left != null && root.left.value == x) || (root.right != null && root.right.value == x)) {
      firstHeight = depth
      firstPV = root.value
    } else if ((root.left != null && root.left.value == y) || (root.right != null && root.right.value == y)) {
      secondHeight = depth
      secondPV = root.value
    }
    val flag = Math.max(firstHeight, secondHeight)
    if (flag > 0 && flag > depth || flag == 0){
      if (root.left != null) findXOrY(root.left, x, y, depth + 1)
      if (root.right != null) findXOrY(root.right, x, y, depth + 1)
    }
  }

  def isCousins(root: TreeNode, x: Int, y: Int): Boolean = {
    findXOrY(root, x, y, 1)
    if (firstHeight != 0 && firstPV != secondPV) {
      firstHeight == secondHeight
    } else false
  }

  def main(args: Array[String]): Unit = {
    val node = new TreeNode(1, new TreeNode(2, null, new TreeNode(4)), new TreeNode(3, null, new TreeNode(5)))
  println(isCousins(node, 4, 5))
  }

}

class TreeNode(_value: Int = 0, _left: TreeNode = null, _right: TreeNode = null) {
    var value: Int = _value
    var left: TreeNode = _left
    var right: TreeNode = _right
  }