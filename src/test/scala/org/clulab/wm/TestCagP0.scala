package org.clulab.wm

import CAG._
import TestUtils._

class TestCagP0 extends Test {
  val tester = new Tester(p0s1)

  behavior of "p0s1"
  
  it should "not be ignored" in {
  }
  
  ignore should "be ignored" taggedAs(Becky) in {
    6 should be (5)
  }
  
  ignore should "still be ignored" taggedAs(Becky, Keith) in {
    5 should be (6)
  }
  
  passingTest should "pass" in {
    1 should be (1)
  }
  
  failingTest should "fail" in {
    1 should be (2)
  }
  
  ignore should "have the correct triples" taggedAs(Somebody) in {
    // Should this just be "rainfall"?
    val inRainfallNode = newNodeSpec("in rainfall", newDecrease("decrease"))
    val povertyNode = newNodeSpec("poverty", newIncrease("increased", "significantly"))
    
    val inRainfallPovertyEdge = newEdgeSpec(inRainfallNode, Causal, povertyNode)
    
    tester.test(inRainfallPovertyEdge) shouldBe (successful)
  }  
}
