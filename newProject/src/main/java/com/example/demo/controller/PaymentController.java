package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.UserRepository.UserRepository;
import com.example.demo.service.payPalService.PayPalService;
import com.example.demo.service.productBillService.BillService;
import com.example.demo.service.userService.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.validation.Valid;
import java.util.HashMap;


@Controller
public class PaymentController {
    public static final String URL_PAYPAL_SUCCESS = "pay/success";
    public static final String URL_PAYPAL_CANCEL = "pay/cancel";

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private PayPalService paypalService;

    @Autowired
    public JavaMailSender emailSender;

    @Autowired
    UserRepository userRepo;

    @Autowired
    UserService userService;

    @Autowired
    BillService billService;

    public static double totalMoney = 0;
    public static int totalQuantity = 0;
    public static ProductBill productDetailBill = new ProductBill();
    public static HashMap<Double, Product> listProductBillTemp = new HashMap<>();
    @ModelAttribute("admin")
    public String AdminOrSaler(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth.getAuthorities().toString().equals("[ROLE_ADMIN]")){
            if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
                return "là admin";
            }
        }else{
            if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().
                    anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_SALER"))) {
                return "là saler";
            }
        }
        return null;
    }
    @ModelAttribute("userNames")
    public AccUser getDauGia() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepo.findByAccount_IdAccount(auth.getName());
    }
    @GetMapping("/bill/getData")
    public String getHoaDon(@RequestParam String total, @RequestParam String quantity,
                            Model model,
                            @SessionAttribute("carts") HashMap<Integer, Cart> cartMap) {
        totalMoney = Integer.parseInt(total);
        totalQuantity = Integer.parseInt(quantity);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("total", total);
        model.addAttribute("quantity", quantity);
        model.addAttribute("accUser",userService.findByAccount(auth.getName()));
        model.addAttribute("carts",cartMap);
        return "Vinh/Pay";
    }

//    @GetMapping("/paypal")
//    public String index(){
//        return "paypal/index";
//    }
//    @PostMapping("/hoaDon/thanhToan")
//    public String pay(HttpServletRequest request, @SessionAttribute("carts") HashMap<Integer, Cart> cartMap, @ModelAttribute Bill donHang,
//                      Model model ){
//        HashMap<Double, Product> listSpHoaDon = new HashMap<>();
//        String temp = "";
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        User user = userService.findByUser_User(auth.getName());
//        LocalDate currentDate = java.time.LocalDate.now();
//        donHang.setDateOder(String.valueOf(currentDate));
//        donHang.setStatus("Đang Giao");
//        donHang.setUser(user);
//        donHang.setTotalCost(totalMoney);
//        billService.create(donHang);
//        ProductBillKey chiTietDonHangKey = new ProductBillKey();
//        ProductBill chiTietDonHang = new ProductBill();
//        for (Map.Entry<Integer, Cart> entry : cartMap.entrySet()) {
//            Cart value = entry.getValue();
//            listSpHoaDon.put(value.getMaxPrice(), value.getProduct());
//            chiTietDonHangKey.setInBill(donHang.getInBill());
//            chiTietDonHangKey.setIdProduct(value.getProduct().getIdProduct());
//            chiTietDonHang.setBill(donHang);
//            chiTietDonHang.setId(chiTietDonHangKey);
//            chiTietDonHang.setProduct(value.getProduct());
//            chiTietDonHang.setQuantity(totalSoLuong);
//            chiTietDonHang.setPrice(value.getMaxPrice());
//            billService.createChiTiet(chiTietDonHang);
//        }
//        chiTietDonHangTemp = chiTietDonHang;
//        listSpHoaDonTemp = listSpHoaDon;
//        String cancelUrl = PaypalUtils.getBaseURL(request) + "/" + URL_PAYPAL_CANCEL;
//        String successUrl = PaypalUtils.getBaseURL(request) + "/" + URL_PAYPAL_SUCCESS;
//        try {
//            Payment payment = paypalService.createPayment(
//                    totalMoney,
//                    "USD",
//                    PaypalPaymentMethod.paypal,
//                    PaypalPaymentIntent.sale,
//                    "payment description",
//                    cancelUrl,
//                    successUrl);
//            for(Links links : payment.getLinks()){
//                if(links.getRel().equals("approval_url")){
//                    return "redirect:" + links.getHref();
//                }
//            }
//        } catch (PayPalRESTException e) {
//            log.error(e.getMessage());
//        }
//        return "redirect:/";
//    }
//    @GetMapping(URL_PAYPAL_CANCEL)
//    public String cancelPay(){
//        return "paypal/cancel";
//    }
//    @GetMapping(URL_PAYPAL_SUCCESS)
//    public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId, Model model, @SessionAttribute("carts") HashMap<Integer, Cart> cartMap){
//        List<String> tenSp = new ArrayList<>();
//        for (Map.Entry<Integer, Cart> entry : cartMap.entrySet()) {
//            Cart value = entry.getValue();
//            tenSp.add(value.getProduct().getProductName());
//        }
//
//        try {
//            Payment payment = paypalService.executePayment(paymentId, payerId);
//            if (payment.getState().equals("approved")) {
//                SimpleMailMessage message = new SimpleMailMessage();
//                message.setFrom("luytlong122@gmail.com");
//                message.setTo(MyConstants.FRIEND_EMAIL);
//                message.setSubject("THÔNG BÁO ĐÃ THANH TOÁN HÓA ĐƠN!");
//                message.setText("Mã hóa đơn: HD" + chiTietDonHangTemp.getBill().getInBill() + "\n" +
//                        "Danh sách sản phẩm: " + tenSp + "\n" +
//                        "Ngày mua: " + chiTietDonHangTemp.getBill().getDateOder() + "\n" +
//                        "Số tiền đã thanh toán: " + totalMoney);
//                this.emailSender.send(message);
//                model.addAttribute("hoaDon", chiTietDonHangTemp);
//                model.addAttribute("listSp", listSpHoaDonTemp);
//                return "nha/HoaDon";
//            }
//        } catch (PayPalRESTException e) {
//            log.error(e.getMessage());
//        }
//        return "redirect:/paypal";
//    }
}
