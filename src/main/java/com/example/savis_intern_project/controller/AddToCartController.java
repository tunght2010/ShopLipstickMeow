package com.example.savis_intern_project.controller;


import com.example.savis_intern_project.entity.*;
import com.example.savis_intern_project.entity.ViewModels.CartDetailView;
import com.example.savis_intern_project.repository.ProductImageResponsitory;
import com.example.savis_intern_project.service.serviceimpl.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class AddToCartController {
    @Autowired
    private HttpSession httpSession;
    @Autowired
    ProductDetailServiceimpl productDetailServiceimpl;
    @Autowired
    ProductImageResponsitory productImageResponsitory;
    @Autowired
    CustomerServiceImpl customerService;
    @Autowired
    CartServiceImpl cartService;
    @Autowired
    CartdetailServiceImpl cartDetailService;

    UUID productId = UUID.fromString("f548c39d-d212-45c3-b191-a2a80f8d9d3b");

    @GetMapping("/cart/add/{id}")
    public String themGioHang(@PathVariable("id") UUID id) {
        ProductDetail productDetail = productDetailServiceimpl.getOne(id);

        if (productDetail == null) {
            // Xử lý trường hợp sản phẩm không tồn tại, ví dụ: thông báo lỗi.
            return "redirect:/error";
        }

        if (httpSession.getAttribute("CustomerName") != null) {
            String username = (String) httpSession.getAttribute("CustomerName");
            Customer customer = customerService.getCustomerByName(username);
            var cart = cartService.getOne(customer.getId());
            var check = cartDetailService.checkExistCartDetail(customer.getId(), id);
            if (check) {
                cart.setTotalMoney(cart.getTotalMoney().add(productDetail.getPrice().multiply(new BigDecimal(productDetail.getQuantity()))));
                cartService.update(cart.getId(), cart);
                var cartDetail = cartDetailService.getOneCartDetail(customer.getId(), id);
                cartDetail.setQuantity(cartDetail.getQuantity() + productDetail.getQuantity());
                cartDetail.setPrice(cartDetail.getPrice().add(productDetail.getPrice().multiply(new BigDecimal(productDetail.getQuantity()))));
                cartDetailService.update(cartDetail.getId(), cartDetail);
            } else {
                cart.setQuantity(cart.getQuantity() + 1);
                cart.setTotalMoney(cart.getTotalMoney().add(productDetail.getPrice().multiply(new BigDecimal(productDetail.getQuantity()))));
                cartService.update(cart.getId(), cart);
                CartDetail cartDetail = new CartDetail();
                cartDetail.setId(UUID.randomUUID());
                cartDetail.setQuantity(productDetail.getQuantity());
                cartDetail.setPrice(productDetail.getPrice().multiply(new BigDecimal(productDetail.getQuantity())));
                cartDetail.setCart(cart);
                cartDetail.setProductDetail(productDetail);
                cartDetailService.save(cartDetail);
            }
        } else {
            ProductImage productImage = productImageResponsitory.findByProductDetailId(id).get(0);
            Cart cartSession = (Cart) httpSession.getAttribute("Cart");

            if (cartSession == null) {
                Cart cart = new Cart();

                UUID customerId = UUID.randomUUID();
                cart.setId(customerId);
                cart.setQuantity(1);
                cart.setTotalMoney(productDetail.getPrice().multiply(new BigDecimal(productDetail.getQuantity())));
                cart.setStatus(0);

                httpSession.setAttribute("Cart", cart);

                ArrayList<CartDetailView> lstCartDetailView = new ArrayList<CartDetailView>();

                CartDetailView cartDetailView = new CartDetailView();

                cartDetailView.setCart(cart);
                cartDetailView.setProductDetail(productDetail);
                cartDetailView.setProductDetailId(productDetail.getId());
                cartDetailView.setProductId(productDetail.getProduct().getId());
                cartDetailView.setId(UUID.randomUUID());
                cartDetailView.setName(productDetail.getProduct().getName());
                cartDetailView.setProductPrice(productDetail.getPrice());
                cartDetailView.setPrice(productDetail.getPrice().multiply(new BigDecimal(productDetail.getQuantity())));
                cartDetailView.setQuantity(productDetail.getQuantity());
                cartDetailView.setColorName(productDetail.getColor().getName());
                cartDetailView.setImage(productImage.getName());

                lstCartDetailView.add(cartDetailView);

                httpSession.setAttribute("CartDetail", lstCartDetailView);
            } else {
                cartSession.setTotalMoney(cartSession.getTotalMoney().add(productDetail.getPrice().multiply(new BigDecimal(productDetail.getQuantity()))));

                ArrayList<CartDetailView> cartDetailSession = (ArrayList<CartDetailView>) httpSession.getAttribute("CartDetail");

                boolean check = false;

                for (CartDetailView cartDetailView : cartDetailSession) {
                    if (cartDetailView.getProductDetailId().equals(id)) {
                        check = true;
                        break;
                    }
                }

                if (check) {
                    httpSession.setAttribute("Cart", cartSession);

                    for (CartDetailView cartDetailView : cartDetailSession) {
                        if (cartDetailView.getProductDetailId().equals(productDetail.getId())) {
                            CartDetailView cartDetailViewOld = cartDetailView;
                            cartDetailSession.remove(cartDetailViewOld);

                            CartDetailView cartDetailViewNew = cartDetailView;
                            cartDetailViewNew.setQuantity(cartDetailView.getQuantity() + productDetail.getQuantity());
                            cartDetailViewNew.setPrice(cartDetailView.getPrice().add(productDetail.getPrice().multiply(new BigDecimal(productDetail.getQuantity()))));
                            cartDetailViewNew.setCart(cartSession);
                            cartDetailSession.add(cartDetailViewNew);
                            break;
                        }
                    }
                } else {
                    cartSession.setQuantity(cartSession.getQuantity() + 1);
                    httpSession.setAttribute("Cart", cartSession);

                    for (CartDetailView cartDetailView : cartDetailSession) {
                        cartDetailView.setCart(cartSession);
                    }

                    CartDetailView cartDetailView = new CartDetailView();

                    cartDetailView.setCart(cartSession);
                    cartDetailView.setProductDetail(productDetail);
                    cartDetailView.setProductDetailId(productDetail.getId());
                    cartDetailView.setProductId(productDetail.getProduct().getId());
                    cartDetailView.setId(UUID.randomUUID());
                    cartDetailView.setName(productDetail.getProduct().getName());
                    cartDetailView.setProductPrice(productDetail.getPrice());
                    cartDetailView.setPrice(productDetail.getPrice().multiply(new BigDecimal(productDetail.getQuantity())));
                    cartDetailView.setQuantity(productDetail.getQuantity());
                    cartDetailView.setColorName(productDetail.getColor().getName());
                    cartDetailView.setImage(productImage.getName());

                    cartDetailSession.add(cartDetailView);
                }
                httpSession.setAttribute("CartDetail", cartDetailSession);
            }
        }
        productDetail.setQuantity(1);
        productDetailServiceimpl.update(id, productDetail);
        return "redirect:/viewOrderCart";
    }

    @GetMapping("/cart/increase/{id}")
    public String IncreaseCart(@PathVariable("id") UUID id) {
        ProductDetail productDetail = productDetailServiceimpl.getOne(id);

        if (productDetail == null) {
            // Xử lý trường hợp sản phẩm không tồn tại, ví dụ: thông báo lỗi.
            return "redirect:/error";
        }

        if (httpSession.getAttribute("CustomerName") != null) {
            String username = (String) httpSession.getAttribute("CustomerName");
            Customer customer = customerService.getCustomerByName(username);
            CartDetail cartDetail = cartDetailService.getOneCartDetail(customer.getId(), id);

            var cart = cartService.getOne(customer.getId());

            cart.setTotalMoney(cart.getTotalMoney().add(productDetail.getPrice()));
            cartService.update(cart.getId(), cart);
            cartDetail.setQuantity(cartDetail.getQuantity() + 1);
            cartDetail.setPrice(cartDetail.getPrice().add(productDetail.getPrice()));
            cartDetailService.update(cartDetail.getId(), cartDetail);
        } else {
            Cart cartSession = (Cart) httpSession.getAttribute("Cart");
            cartSession.setTotalMoney(cartSession.getTotalMoney().add(productDetail.getPrice()));
            httpSession.setAttribute("Cart", cartSession);

            ArrayList<CartDetailView> cartDetailSession = (ArrayList<CartDetailView>) httpSession.getAttribute("CartDetail");

            if (cartDetailSession == null) {
                return "redirect:/viewOrderCart";
            }

            for (CartDetailView cartDetailView : cartDetailSession) {
                if (cartDetailView.getProductDetailId().equals(productDetail.getId())) {
                    CartDetailView cartDetailViewOld = cartDetailView;
                    cartDetailSession.remove(cartDetailViewOld);

                    CartDetailView cartDetailViewNew = cartDetailView;
                    cartDetailViewNew.setQuantity(cartDetailView.getQuantity() + 1);
                    cartDetailViewNew.setPrice(cartDetailView.getPrice().add(productDetail.getPrice()));
                    cartDetailViewNew.setCart(cartSession);
                    cartDetailSession.add(cartDetailViewNew);
                    break;
                }
            }
            httpSession.setAttribute("CartDetail", cartDetailSession);
        }
        return "redirect:/viewOrderCart";
    }

    @GetMapping("/cart/reduce/{id}")
    public String ReduceCart(@PathVariable("id") UUID id) {
        ProductDetail productDetail = productDetailServiceimpl.getOne(id);

        if (productDetail == null) {
            // Xử lý trường hợp sản phẩm không tồn tại, ví dụ: thông báo lỗi.
            return "redirect:/error";
        }

        if (httpSession.getAttribute("CustomerName") != null) {
            String username = (String) httpSession.getAttribute("CustomerName");
            Customer customer = customerService.getCustomerByName(username);
            CartDetail cartDetail = cartDetailService.getOneCartDetail(customer.getId(), id);

            var cart = cartService.getOne(customer.getId());

            cart.setTotalMoney(cart.getTotalMoney().subtract(productDetail.getPrice()));
            if (cartDetail.getQuantity() > 1) {
                cartDetail.setQuantity(cartDetail.getQuantity() - 1);
                cartDetail.setPrice(cartDetail.getPrice().subtract(productDetail.getPrice()));
                cartDetailService.update(cartDetail.getId(), cartDetail);
            } else {
                cartDetailService.delete(cartDetail.getId());
                cart.setQuantity(cart.getQuantity() - 1);
            }
            cartService.update(cart.getId(), cart);
        } else {
            Cart cartSession = (Cart) httpSession.getAttribute("Cart");
            cartSession.setTotalMoney(cartSession.getTotalMoney().subtract(productDetail.getPrice()));
            httpSession.setAttribute("Cart", cartSession);

            ArrayList<CartDetailView> cartDetailSession = (ArrayList<CartDetailView>) httpSession.getAttribute("CartDetail");

            if (cartDetailSession == null) {
                return "redirect:/viewOrderCart";
            }
            for (CartDetailView cartDetailView : cartDetailSession) {
                if (cartDetailView.getProductDetailId().equals(productDetail.getId())) {
                    CartDetailView cartDetailViewOld = cartDetailView;
                    cartDetailSession.remove(cartDetailViewOld);
                    if (cartDetailView.getQuantity() > 1) {
                        CartDetailView cartDetailViewNew = cartDetailView;
                        cartDetailViewNew.setQuantity(cartDetailView.getQuantity() - 1);
                        cartDetailViewNew.setPrice(cartDetailView.getPrice().subtract(productDetail.getPrice()));
                        cartDetailViewNew.setCart(cartSession);
                        cartDetailSession.add(cartDetailViewNew);
                        break;
                    } else if (cartDetailView.getQuantity() <= 1 && cartSession.getQuantity() > 1) {
                        httpSession.invalidate();
                    } else {
                        cartSession.setQuantity(cartSession.getQuantity() - 1);
                        httpSession.setAttribute("Cart", cartSession);
                        break;
                    }
                }
            }
        }
        return "redirect:/viewOrderCart";
    }

    @GetMapping("/cart/delete/{id}")
    public String deleteCart(@PathVariable("id") UUID id) {
        ProductDetail productDetail = productDetailServiceimpl.getOne(id);

        if (productDetail == null) {
            // Xử lý trường hợp sản phẩm không tồn tại, ví dụ: thông báo lỗi.
            return "redirect:/error";
        }

        if (httpSession.getAttribute("CustomerName") != null) {
            String username = (String) httpSession.getAttribute("CustomerName");
            Customer customer = customerService.getCustomerByName(username);
            CartDetail cartDetail = cartDetailService.getOneCartDetail(customer.getId(), id);

            var cart = cartService.getOne(customer.getId());

            cart.setQuantity(cart.getQuantity() - 1);
            cart.setTotalMoney(cart.getTotalMoney().subtract(cartDetail.getPrice()));
            cartService.update(cart.getId(), cart);
            cartDetailService.delete(cartDetail.getId());
        } else {
            Cart cartSession = (Cart) httpSession.getAttribute("Cart");
            ArrayList<CartDetailView> cartDetailSession = (ArrayList<CartDetailView>) httpSession.getAttribute("CartDetail");

            if (cartDetailSession == null) {
                return "redirect:/viewOrderCart";
            }

            if (cartSession.getQuantity() > 1) {
                for (CartDetailView cartDetailView : cartDetailSession) {
                    if (cartDetailView.getProductDetailId().equals(productDetail.getId())) {
                        CartDetailView cartDetailViewOld = cartDetailView;
                        cartDetailSession.remove(cartDetailViewOld);

                        cartSession.setQuantity(cartSession.getQuantity() - 1);
                        cartSession.setTotalMoney(cartSession.getTotalMoney().subtract(cartDetailViewOld.getPrice()));
                        httpSession.setAttribute("Cart", cartSession);
                        break;
                    }
                }
            } else {
                httpSession.invalidate();
            }
        }
        return "redirect:/viewOrderCart";
    }

    @GetMapping("/cart/deleteAll")
    public String deleteAllCart() {
        if (httpSession.getAttribute("CustomerName") != null) {
            List<CartDetail> cartDetail = cartDetailService.getAll();

            if (cartDetail == null) {
                // Xử lý trường hợp sản phẩm không tồn tại, ví dụ: thông báo lỗi.
                return "redirect:/error";
            }

            String username = (String) httpSession.getAttribute("CustomerName");
            Customer customer = customerService.getCustomerByName(username);

            var cart = cartService.getOne(customer.getId());

            cart.setQuantity(0);
            cart.setTotalMoney(BigDecimal.ZERO);
            cartService.update(cart.getId(), cart);
            cartDetailService.deleteAll(customer.getId());
        } else {
            httpSession.invalidate();
        }
        return "redirect:/viewOrderCart";
    }

    @GetMapping("/viewOrderCart")
    public String showCartItem(Model model) {
        if (httpSession.getAttribute("CustomerName") != null) {
            String username = (String) httpSession.getAttribute("CustomerName");
            Customer customer = customerService.getCustomerByName(username);
            model.addAttribute("cart", cartService.getOne(customer.getId()));
            model.addAttribute("listCartDetail", cartDetailService.getCartDetailByCustomerId(customer.getId()));
        } else {
            Cart cartSession = (Cart) httpSession.getAttribute("Cart");
            ArrayList<CartDetailView> cartDetailSession = (ArrayList<CartDetailView>) httpSession.getAttribute("CartDetail");

            model.addAttribute("cart", cartSession);
            model.addAttribute("listCartDetail", cartDetailSession);
        }
        model.addAttribute("listProductDetail", productDetailServiceimpl.getAll());
        model.addAttribute("view", "/cart/index.jsp");
        return "/customerFE/index";
    }

    @GetMapping("/add/{id}")
    public String themGioHang1(@PathVariable("id") UUID id) {
        ProductDetail productDetail = productDetailServiceimpl.getOne(id);
        OrderCart cartSession = (OrderCart) httpSession.getAttribute("OrderCart");
        if (productDetail == null) {
            // Xử lý trường hợp sản phẩm không tồn tại, ví dụ: thông báo lỗi.
            return "redirect:/error";
        }

        UUID productId1 = productDetail.getId();
        String tenSanPham = productDetail.getProduct().getName();
        BigDecimal price = productDetail.getPrice();
        BillDetail item = new BillDetail();
        item.setPrice(price);
        item.setProductDetail(productDetailServiceimpl.getOne(productId1));
//        item.setBill(b);
        item.setQuantity(1);

        if (cartSession == null) {
            OrderCart cart = new OrderCart();
            ArrayList<BillDetail> list = new ArrayList<>();
            list.add(item);
            cart.setBillDetails(list);
            httpSession.setAttribute("OrderCart", cart);
        } else {
            if (cartSession == null) {
                // Xử lý trường hợp giỏ hàng không tồn tại, ví dụ: thông báo lỗi.
                return "redirect:/error";
            }

            ArrayList<BillDetail> listItem = cartSession.getBillDetails();
            if (listItem == null) {
                // Tạo danh sách mới nếu nó chưa tồn tại.
                listItem = new ArrayList<>();
            }

            for (BillDetail itemTmp : listItem) {
                if (itemTmp.getProductDetail().getId().equals(productId1)) {
                    itemTmp.setQuantity(itemTmp.getQuantity() + 1);
                    itemTmp.setPrice(price.multiply(BigDecimal.valueOf(itemTmp.getQuantity())));
                    return "redirect:/viewOrderCart";
                }
            }
            listItem.add(item);
            cartSession.setBillDetails(listItem);
        }
        return "redirect:/viewOrderCart";
    }
}
