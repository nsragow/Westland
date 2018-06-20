package westland.signature.automator;

public class SignatureGenerator
{
  static String one = "<table width=\"100%\" cellspacing=\"10\" border=\"0\"><tbody><tr><td colspan=\"0\" width=\"158\" align=\"left\"><img src=\"http://westlandrealestategroup.com/images/CORPLogoEmailSig150px.jpg\"></td><td width=\"100%\"><p><b>";
  static String two = "</b><br><a style=\"color:#000000;font-size:10px;font-family:&#39;Gill Sans&#39;,&#39;Gill Sans MT&#39;,Cambria,&#39;Hoefler Text&#39;,&#39;Liberation Serif&#39;,Times,&#39;Times New Roman&#39;,serif;text-decoration:none!important\">";
  static String three = "<br>";
  static String four = "<br><a style=\"color:#000000;font-size:11px;font-family:&#39;Gill Sans&#39;,&#39;Gill Sans MT&#39;,Cambria,&#39;Hoefler Text&#39;,&#39;Liberation Serif&#39;,Times,&#39;Times New Roman&#39;,serif;text-decoration:none!important\">";
  static String five = "</a></a></p><a style=\"color:#000000;font-size:12px;font-family:&#39;Gill Sans&#39;,&#39;Gill Sans MT&#39;,Cambria,&#39;Hoefler Text&#39;,&#39;Liberation Serif&#39;,Times,&#39;Times New Roman&#39;,serif;text-decoration:none!important\"></a><p style=\"font-size:12px;font-family:&#39;Gill Sans&#39;,&#39;Gill Sans MT&#39;,Cambria,&#39;Hoefler Text&#39;,&#39;Liberation Serif&#39;,Times,&#39;Times New Roman&#39;,serif\"><a style=\"color:#000000;font-size:12px;font-family:&#39;Gill Sans&#39;,&#39;Gill Sans MT&#39;,Cambria,&#39;Hoefler Text&#39;,&#39;Liberation Serif&#39;,Times,&#39;Times New Roman&#39;,serif;text-decoration:none!important\">";
  static String six = "<br></a><a style=\"color:#000000;font-size:10px;font-family:&#39;Gill Sans&#39;,&#39;Gill Sans MT&#39;,Cambria,&#39;Hoefler Text&#39;,&#39;Liberation Serif&#39;,Times,&#39;Times New Roman&#39;,serif;text-decoration:none!important\">";
  static String seven = "<br>";
  static String eight = "<br></a><a style=\"color:#c62e2f;font-size:12px\">";
  static String nine = "</a></p></td></tr></tbody></table>";
  public static String makeSignature(String name, String title, String faxAndPhone, String email, String company, String firstPartAdd, String secondPartAdd, String website)
  {
    StringBuilder builder = new StringBuilder();
    builder.append(one);builder.append(name);builder.append(two);
    builder.append(title);builder.append(three);builder.append(faxAndPhone);
    builder.append(four);builder.append(email);builder.append(five);
    builder.append(company);builder.append(six);builder.append(firstPartAdd);
    builder.append(seven);builder.append(secondPartAdd);builder.append(eight);
    builder.append(website);builder.append(nine);
    return builder.toString();
  }
  public static String makeSignature(SignatureBuilder sB){
    if(!sB.isComplete()){
      throw new IllegalArgumentException("the SignatureBuilder must be complete before it can be made into a signature");
    }
    sB.removeNulls();
    return makeSignature(sB.get("name"),sB.get("title"),sB.getPhoneHTML(),sB.get("email"),sB.get("org"),sB.get("addressPartOne"),sB.get("addressPartTwo"),sB.get("website"));
  }
}
