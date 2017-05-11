package com.lifeway.it.user

import groovyx.net.http.HTTPBuilder

import static java.util.UUID.randomUUID

/**
 * Created by sford on 5/11/17.
 */
class UserGenerator {

   def static userService = new HTTPBuilder("https://bspuser.uat.lifeway.com")
   def static paymentMethodService = new HTTPBuilder("https://paymentmethodservice.uat.lifeway.com")

   public static void createUsers(int start, int count, String filename){
      println "Building ${count - start +1} test users"
      List<String> users = new ArrayList<>()
      String authStringEnc = Base64.encoder.encodeToString("SOA:muletest".getBytes())
      String paymentUserAuth = Base64.encoder.encodeToString("paymentUser:secret".getBytes())
      userService.setHeaders([Authorization: "Basic $authStringEnc"])
      paymentMethodService.setHeaders([Authorization: "Basic $paymentUserAuth"])
      start.upto(count) {
         String uuid = randomUUID()
         String email = "dotcom.tester$it@lifeway.com"
         if(createUser(it, uuid, email)){
            createPaymentMethod(uuid)
            users.add("$email, $uuid")
         }

         if(users.size() > 0){
            writeFile(users, filename)
         }
      }
   }

   private static boolean createUser(int count, String uuid, String email ){
      println "Creating user"
      userService.post(path:"/v1/users", body: getBodyXml(count, uuid, email),
            requestContentType: "application/xml") {resp ->
         println resp.statusLine
         return resp.statusLine.statusCode == 201
      }
   }

   private static void createPaymentMethod(String uuid) {
      println "Creating payment method"
      paymentMethodService.post(path: "/paymentMethods/persons/creditcards", body: getCreditCardJson(uuid),
            requestContentType: "application/json") { resp ->
         println resp.statusLine
         return resp.statusLine.statusCode == 201
      }
   }

   private static String getBodyXml(int count, String uuid, String email){
      return "<users>\n" +
            "    <user>\n" +
            "        <id>$uuid</id>\n" +
            "        <denomination>Baptist - Southern Baptist</denomination>\n" +
            "        <email>\n" +
            "            <emailAddress>$email</emailAddress>\n" +
            "            <type>personal</type>\n" +
            "        </email>\n" +
            "        <firstName>dotcom</firstName>\n" +
            "        <lastName>Tester</lastName>\n" +
            "        <phone>\n" +
            "            <type>work</type>\n" +
            "            <phoneNumber>(615) 222-2222</phoneNumber>\n" +
            "        </phone>\n" +
            "        <over13Flag>true</over13Flag>\n" +
            "        <login>\n" +
            "            <id>$uuid</id>\n" +
            "            <displayName>dotcom.tester$count</displayName>\n" +
            "            <username>$email</username>\n" +
            "            <password>secret123</password>\n" +
            "        </login>\n" +
            "    </user>\n" +
            "</users>"
   }

   private static String getCreditCardJson(String uuid){
      long date = Calendar.getInstance().timeInMillis
      return "{\n" +
            "  \"personProfileId\" : \"$uuid\",\n" +
            "  \"displayName\" : \"tester card\",\n" +
            "  \"cardHolderName\" : \"Dotcom Tester\",\n" +
            "  \"token\" : \"84400006503800264\",\n" +
            "  \"cardNumberMask\" : \"**** **** **** 1234\",\n" +
            "  \"expirationMonth\" : 9,\n" +
            "  \"expirationYear\" : 2017,\n" +
            "  \"address\" : {\n" +
            "    \"firstName\": \"Dotcom\",\n" +
            "    \"lastName\": \"Tester\",\n" +
            "    \"line1\": \"1234 10 Ave N.\",\n" +
            "    \"city\": \"Nashville\",\n" +
            "    \"countrySubdivision\": \"TN\",\n" +
            "    \"postalCode\": \"37211\",\n" +
            "    \"countryCode\": \"US\",\n" +
            "    \"phoneNumber\": \"5555555555\",\n" +
            "    \"defaultAddress\": false,\n" +
            "    \"category\": \"UNKNOWN\",\n" +
            "    \"type\": \"UNKNOWN\"\n" +
            "  }\n" +
            "}"
   }

   private static void writeFile(List<String> users, String fileName){
      File testUserFile = new File(fileName)
      testUserFile.withWriter{ out ->
         users.each {out.println it}
      }
      testUserFile
      println testUserFile.exists()
      println testUserFile.absolutePath
   }
}
