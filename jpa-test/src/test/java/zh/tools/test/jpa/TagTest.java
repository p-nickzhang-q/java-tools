package zh.tools.test.jpa;

import lombok.Data;
import zh.tools.common.bitmap.MultiIntMap;
import zh.tools.common.bitmap.OneIntMap;

public class TagTest {

    private enum Tag {
        vip, mobile, email, male, mac, superVip, lost
    }

    @Data
    private static class User {
        private int i = 9;

        private String i2;

    }


    public static void main(String[] args) {
        User user = new User();
        OneIntMap oneIntMap = new OneIntMap(user, "i");
        oneIntMap.add(Tag.mac.ordinal());
        oneIntMap.add(Tag.superVip.ordinal());
        System.out.println(oneIntMap.getValues(Tag.values()));
        System.out.println(user.getI());
        MultiIntMap multiIntMap = new MultiIntMap(10, user, "i2");
        multiIntMap.add(Tag.mac.ordinal());
        multiIntMap.add(Tag.vip.ordinal());
        System.out.println(multiIntMap.getValues(Tag.values()));
        System.out.println(user.getI2());
    }
}
