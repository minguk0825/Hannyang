package com.example.hannyang.product;

import com.example.hannyang.member.Member;
import com.example.hannyang.member.MemberRepository;
import com.example.hannyang.s3.S3ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final MemberRepository memberRepository;

    private final S3ClientService s3ClientService;


    // 전체 리스트 조회
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 상품 타입별 리스트 조회
    public List<Product> getProductsByCategory(ProductType type) {
        return productRepository.findByType(type);
    }

    // 상품 구매
    @Transactional
    public void purchaseProduct(Long memberId, Long productId, int quantity) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID"));

        // 총 구매 비용 계산
        int totalCost = product.getPrice() * quantity;

        if (member.getPoints() >= totalCost && product.getQuantity() >= quantity) {
            member.setPoints(member.getPoints() - totalCost); // 포인트 차감
            memberRepository.save(member);

            product.setQuantity(product.getQuantity() - quantity); // 상품 수량 감소
            productRepository.save(product);

            // 구매 로직 추가 (예: 구매 내역 저장 등)
        } else {
            throw new IllegalArgumentException("Insufficient points or product quantity");
        }
    }

    // 이미지 Url과 함께 상품 정보 저장 (초기 등록)
    public Product createProductWithImage(Product product, MultipartFile imageFile) throws IOException {
        // 상품 정보 저장
        product = productRepository.save(product);

        // 이미지 파일을 S3에 업로드하고, URL을 상품 정보에 추가
        if (imageFile != null && !imageFile.isEmpty()) {
            String bucketName = "your-s3-bucket-name";
            String keyName = "product-images/" + product.getId() + "-" + imageFile.getOriginalFilename();
            Path tempFile = convertMultipartFileToFile(imageFile);

            s3ClientService.uploadFile(bucketName, keyName, tempFile);
            String imageUrl = s3ClientService.getFileUrl(bucketName, keyName);
            product.setImageUrl(imageUrl);

            Files.delete(tempFile); // 임시 파일 삭제
        }

        return productRepository.save(product);
    }

    // MultipartFile을 로컬 파일로 변환하는 메서드
    private Path convertMultipartFileToFile(MultipartFile file) throws IOException {
        Path tempFile = Files.createTempFile("upload-", file.getOriginalFilename());
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

    // 상품명으로 상품 갯수 증가
    @Transactional
    public Product increaseProductQuantityByName(String name, int quantity) {
        Product product = productRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product name"));

        product.increaseQuantity(quantity);
        return product;
    }
}

