package com.example.savis_intern_project.controller;

import com.example.savis_intern_project.entity.Color;
import com.example.savis_intern_project.entity.Product;
import com.example.savis_intern_project.entity.ProductDetail;
import com.example.savis_intern_project.entity.ProductImage;
import com.example.savis_intern_project.repository.ColorResponsitory;
import com.example.savis_intern_project.repository.ProductDetailResponsitory;
import com.example.savis_intern_project.repository.ProductResponsitory;
import com.example.savis_intern_project.service.ProductImageService;
import com.example.savis_intern_project.service.serviceimpl.ColorServiceimpl;
import com.example.savis_intern_project.service.serviceimpl.ProductDetailServiceimpl;
import com.example.savis_intern_project.service.serviceimpl.ProductServiceimpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.UUID;

@Controller
@RequestMapping("/product_image")
public class ProductImageController {
    @Autowired
    private ProductServiceimpl productServiceimpl;
    @Autowired
    private ProductImageService productImageService;
    @Autowired
    private ProductDetailServiceimpl productDetailServiceimpl;
    @Autowired
    private ProductResponsitory productResponsitory;
    @Autowired
    private ProductDetailResponsitory productDetailResponsitory;
    @GetMapping("/index")
    public String hienThi(Model model){
        model.addAttribute("listProduct",productServiceimpl.getAll());
        model.addAttribute("listProductDetail",productDetailServiceimpl.getAll());
        model.addAttribute("listProductImage",productImageService.getAll());
        model.addAttribute("ProductImage",new ProductImage());
        model.addAttribute("view", "/ProductImage/index.jsp");
        return "index";
    }

//    @GetMapping("/indexcus" )
//    public String show_data_product_cus(Model model){
//
//        model.addAttribute("listProduct",productServiceimpl.getAll());
//        model.addAttribute("Product",new Product());
//        model.addAttribute("listCategory",categoryServiceimpl.getAll());
//        model.addAttribute("listColor",colorServiceimpl.getAll());
//        model.addAttribute("view", "/product/index.jsp");
//        return "/customerFE/index";
//    }

    @PostMapping("/add")
    public String add(Model model,
                      @RequestParam("name") String name,
                      @RequestParam("staTus") Integer staTus,
                      @RequestParam("productDetail")String productDetail
    ){

        ProductDetail productDetail1 = productDetailResponsitory.findById(UUID.fromString(productDetail)).orElse(null);

        ProductImage productImage = new ProductImage(name,staTus,productDetail1);
        productImageService.add(productImage);

        return "redirect:/product_image/index";
    }
    @GetMapping("/detail/{id}")
    public String detail(Model model,
                         @PathVariable("id") UUID id){
        model.addAttribute("listProduct",productServiceimpl.getAll());
        model.addAttribute("listProductDetail",productDetailServiceimpl.getAll());
        model.addAttribute("listProductImage",productImageService.getAll());
        model.addAttribute("imageSP",productImageService.getOne(id));
        model.addAttribute("view", "/ProductImage/index.jsp");
        return "index";
    }
    @GetMapping("/delete/{id}")
    public String delete(Model model,
                         @PathVariable("id") UUID id){
        productImageService.delete(id);
        return "redirect:/product_image/index";
    }
    @PostMapping("/update/{id}")
    public String update(Model model,
                         @PathVariable("id") UUID id,
                         @ModelAttribute("productImage") ProductImage productImage){
        productImageService.update(id,productImage);
        return "redirect:/product_image/index";
    }
}