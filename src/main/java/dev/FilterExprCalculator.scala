package dev

import java.util
import java.util.regex.Pattern
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.read
import scala.collection.mutable.ArrayBuffer

/**
 * 实验参数表达式计算核心逻辑
 */
object FilterExprCalculator {

  private val EXPR_CALC_SINGLE_VALUE_OP_LIST = List("!")

  private val EXPR_CALC_DOUBLE_VALUE_OP_LIST = List("&", "|")


  /**
   * 解析配置参数过滤规则
   * @param strategyStr 过滤规则表达式
   * @return 后缀表达式过滤器
   */
  def parseStrategyParams(strategyStr: String): Option[StrategyExpr] = {
    implicit val defaultFormats = DefaultFormats
    if (strategyStr.isEmpty) {
      println(s"Filter strategyStr is empty! Nothing will be filtered!")
      None
    } else {
      val strategyParams = read[StrategyParams](strategyStr)
      val ruleMap = strategyParams.filter_rules.map(item => item.id -> item).toMap
      Some(StrategyExpr(ruleMap, parseExpr(strategyParams.rule_expr)))
    }
  }

  /**
   * 过滤规则表达式解析: 中缀表达式 -> 后缀表达式
   * @param expr 中缀表达式 eg. !(R4 | R3 & R5) | !R6 & !(R1 & !R2)
   * @return 后缀表达式数组 eg. Array("R4", "R3", "|", "R5", "&", "!", "R6", "!", "|", "R1", "R2", "!", "&", "!", "&")
   */
  def parseExpr(expr: String): Array[String] = {
    // 后缀表达式
    val suffixExpr = ArrayBuffer.empty[String]
    val exprStr = expr.replaceAll(" ", "")
    var temp = new StringBuffer()
    // 运算符栈
    val symbolStack: util.Stack[String] = new util.Stack[String]
    exprStr.foreach{ ch =>
      // 普通规则标识符
      if (isExprRuleId(ch)) {
        temp.append(ch)
      } else {
        // 规则标识符入栈
        if (temp.toString.nonEmpty) {
          suffixExpr += temp.toString
          temp = new StringBuffer()
        }
        // 操作符处理
        if (ch == ')') {
          var scanFlag = true
          while(scanFlag && !symbolStack.isEmpty) {
            val op = symbolStack.pop()
            if (op == "(") scanFlag = false else suffixExpr += op
          }
        } else if (ch == '(' || symbolStack.isEmpty) {
          symbolStack.push(ch.toString)
        } else if ( !symbolStack.isEmpty && symbolStack.peek() != "(") {
          while(!symbolStack.isEmpty && symbolStack.peek() != "(" && comparePriority(ch.toString) >= comparePriority(symbolStack.peek())) {
            suffixExpr += symbolStack.pop()
          }
          symbolStack.push(ch.toString)
        } else {
          symbolStack.push(ch.toString)
        }
      }
    }
    // 最后一个元素是标识符
    if (temp.toString.nonEmpty) {
      suffixExpr += temp.toString
      temp = new StringBuffer()
    }
    // 操作符栈不为空
    while(!symbolStack.isEmpty) {
      suffixExpr += symbolStack.pop()
    }
    println(s"Parse Filter rule_expr to suffix_expr,\n\trule_expr =>\n\t\t$expr\n\tsuffix_expr =>\n\t\t${suffixExpr.mkString(" ")}")
    suffixExpr.toArray
  }

  /**
   * 规则过滤匹配逻辑
   * @param strategy 过滤规则参数
   * @param valueMap 匹配map
   * @return true/false
   */
  def calculate(strategy: StrategyExpr, valueMap: Map[String, AnyVal]): Boolean = {
    // 运算中间结果存储栈
    val calcStack: util.Stack[Boolean] = new util.Stack[Boolean]
    strategy.suffixExpr.foreach{ expr =>
      val calcV = expr match {
        case s if EXPR_CALC_SINGLE_VALUE_OP_LIST.contains(s) =>
          // 单值操作符
          val topBoolean = calcStack.pop()
          s match {
            case "!" => !topBoolean
            case _ => throw new IllegalArgumentException(s"Can't recognize single value op: $s")
          }
        case d if EXPR_CALC_DOUBLE_VALUE_OP_LIST.contains(d) =>
          // 双值操作符
          val (top1, top2) = (calcStack.pop(), calcStack.pop)
          d match {
            case "|" => top2 || top1
            case "&" => top2 && top1
            case _ => throw new IllegalArgumentException(s"Can't recognize double value op: $d")
          }
        case ruleId =>
          // ruleId获取过滤规则结果
          val filterRule = strategy.ruleMap.getOrElse(ruleId,
            throw new IllegalArgumentException(s"Lost rule_id $ruleId in strategy, strategy ruleSuffixExpr: ${strategy.suffixExpr}"))
          if (valueMap.contains(filterRule.dim)) {
            val filterRuleRes = filterRule.value.contains(valueMap.getOrElse(filterRule.dim, ""))
            filterRule.op.toUpperCase.trim match {
              case "IN" => filterRuleRes
              case "NOT IN" => !filterRuleRes
              case _ => throw new IllegalArgumentException(s"Can't recognize config filter_rules.op: ${filterRule.op}")
            }
          } else true // 数据map中没有该key默认返回true
      }
      calcStack.push(calcV)
    }
    assert(calcStack.size() == 1, throw new Exception(s"FilterExpr calc error. calcStack size != 1, ruleSuffixExpr: ${strategy.suffixExpr}"))
    calcStack.pop()
  }

  private val pattern = Pattern.compile("[0-9a-zA-Z_$]")

  /**
   * 属于标识符id
   * @param ch 当前判定字符属于 字母/数字/下划线/$
   * @return true/false
   */
  private def isExprRuleId(ch: Char): Boolean = {
    pattern.matcher(ch.toString).matches()
  }

  /**
   * 运算符优先级
   * @param op 操作符
   * @return 优先级 (1 > 0 > -1)
   */
  private def comparePriority(op: String): Int = {
    op match {
      case c if EXPR_CALC_DOUBLE_VALUE_OP_LIST.contains(c) => 1
      case c if EXPR_CALC_SINGLE_VALUE_OP_LIST.contains(c) => 0
      case _ => -1
    }
  }

}

case class ExpFilterRule(id: String, op: String, dim: String, value: Array[AnyVal])

case class StrategyExpr(ruleMap: Map[String, ExpFilterRule], suffixExpr: Array[String])

case class StrategyParams(filter_rules: Array[ExpFilterRule], rule_expr: String)