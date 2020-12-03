package dev.galasa.api.ras;

import java.time.Instant;
import java.util.List;


public class TestMethod {
   private String className;
   private String methodName;
   private String type;
   private String status;
   private String result;
   private Instant startTime;
   private Instant endTime;
   private int runLogStart;
   private int runLogEnd;
   private List<TestMethod> befores;
   private List<TestMethod> afters;
   
   public TestMethod(String className, String methodName, String type, String status, String result, 
         Instant startTime, Instant endTime, int runLogStart, int runLogEnd, List<TestMethod> befores, List<TestMethod> afters) {
      this.className = className;
      this.methodName = methodName;
      this.type = type;
      this.status = status;
      this.result = result;
      this.startTime = startTime;
      this.endTime = endTime;
      this.runLogStart = runLogStart;
      this.runLogEnd = runLogEnd;
      this.befores = befores;
      this.afters = afters;
   }

   public String getClassName() {
      return className;
   }

   public void setClassName(String className) {
      this.className = className;
   }

   public String getMethodName() {
      return methodName;
   }

   public void setMethodName(String methodName) {
      this.methodName = methodName;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public String getResult() {
      return result;
   }

   public void setResult(String result) {
      this.result = result;
   }

   public Instant getStartTime() {
      return startTime;
   }

   public void setStartTime(Instant startTime) {
      this.startTime = startTime;
   }

   public Instant getEndTime() {
      return endTime;
   }

   public void setEndTime(Instant endTime) {
      this.endTime = endTime;
   }

   public int getRunLogStart() {
      return runLogStart;
   }

   public void setRunLogStart(int runLogStart) {
      this.runLogStart = runLogStart;
   }

   public int getRunLogEnd() {
      return runLogEnd;
   }

   public void setRunLogEnd(int runLogEnd) {
      this.runLogEnd = runLogEnd;
   }

   public List<TestMethod> getBefores() {
      return befores;
   }

   public void setBefores(List<TestMethod> befores) {
      this.befores = befores;
   }

   public List<TestMethod> getAfters() {
      return afters;
   }

   public void setAfters(List<TestMethod> afters) {
      this.afters = afters;
   }
   
   
}
