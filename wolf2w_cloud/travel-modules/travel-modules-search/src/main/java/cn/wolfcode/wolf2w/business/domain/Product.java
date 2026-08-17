package cn.wolfcode.wolf2w.business.domain;

import lombok.Data;

@Data
public class Product {

    private Long id;
    private String title;
    private String intro;
    private Integer price;
    private String brand;
}
