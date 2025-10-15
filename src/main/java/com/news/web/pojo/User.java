package com.news.web.pojo;



import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
//lombok  在编译阶段,为实体类自动生成setter  getter toString
// pom文件中引入依赖   在实体类上添加注解
@Data
public class User {
    @NotNull
    private Integer userId;//主键ID
    private Integer qqId;//qq号
    private String username;//用户名
    @JsonIgnore//让springmvc把当前对象转换成json字符串的时候,忽略password,最终的json字符串中就没有password这个属性了
    private String password;//密码

    private LocalDateTime createTime;//创建时间
    private LocalDateTime updateTime;//更新时间
}