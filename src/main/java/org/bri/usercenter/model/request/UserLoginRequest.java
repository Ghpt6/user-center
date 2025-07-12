package org.bri.usercenter.model.request;

import lombok.Data;

/**用户登录请求体
 *
 */
@Data
public class UserLoginRequest implements java.io.Serializable {
    private static final long serialVersionUID = 2354682342348927934L;

    private String userAccount;
    private String userPassword;
}
