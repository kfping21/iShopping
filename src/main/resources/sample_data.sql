-- 使用数据库
USE ishopping;

-- 插入管理员用户 (密码: admin123)
INSERT INTO users (username, password, email, phone, role, balance, points) VALUES
    ('admin', '$2a$10$ABC123def456ghI789JKLm.ns2VQZ1X2Y3Z4a5b6c7d8e9f0g1h2i3j', 'admin@ishopping.com', '13800138000', 'ADMIN', 10000.00, 1000);

-- 插入商家用户 (密码: seller123)
INSERT INTO users (username, password, email, phone, role, balance, points) VALUES
                                                                                ('tech_store', '$2a$10$DEF456abc789ghI012JKLm.ns2VQZ1X2Y3Z4a5b6c7d8e9f0g1h2i3j', 'tech@ishopping.com', '13900139000', 'SELLER', 5000.00, 500),
                                                                                ('fashion_shop', '$2a$10$GHI789def012jkL345MNOn.s2VQZ1X2Y3Z4a5b6c7d8e9f0g1h2i3j', 'fashion@ishopping.com', '13700137000', 'SELLER', 3000.00, 300),
                                                                                ('book_store', '$2a$10$JKL012ghi345mnO678PQRs.t2VQZ1X2Y3Z4a5b6c7d8e9f0g1h2i3j', 'books@ishopping.com', '13600136000', 'SELLER', 2000.00, 200);

-- 插入普通用户 (密码: user123)
INSERT INTO users (username, password, email, phone, role, balance, points, shipping_address) VALUES
                                                                                                  ('zhangsan', '$2a$10$MNO345jkl678pqR901STUv.u2VQZ1X2Y3Z4a5b6c7d8e9f0g1h2i3j', 'zhangsan@email.com', '13500135001', 'CUSTOMER', 1000.00, 100, '北京市朝阳区建国路100号'),
                                                                                                  ('lisi', '$2a$10$PQR678mno901stU234VWXw.v2VQZ1X2Y3Z4a5b6c7d8e9f0g1h2i3j', 'lisi@email.com', '13500135002', 'CUSTOMER', 800.00, 80, '上海市浦东新区陆家嘴金融中心'),
                                                                                                  ('wangwu', '$2a$10$STU901opq234vwX567YZAb.w2VQZ1X2Y3Z4a5b6c7d8e9f0g1h2i3j', 'wangwu@email.com', '13500135003', 'CUSTOMER', 1500.00, 150, '广州市天河区珠江新城');

-- 插入电子产品
INSERT INTO products (name, description, price, original_price, discount, seller_id, stock, sales, image_url, category, tags, rating, review_count) VALUES
                                                                                                                                                        ('iPhone 15 Pro', '最新款iPhone，搭载A17 Pro芯片', 7999.00, 8999.00, 0.89, 2, 50, 120, '/images/iphone15.jpg', '手机数码', '苹果,手机,5G', 4.8, 45),
                                                                                                                                                        ('MacBook Pro 14英寸', 'M3芯片，性能强劲的笔记本电脑', 12999.00, 14999.00, 0.87, 2, 30, 80, '/images/macbook.jpg', '电脑办公', '苹果,笔记本,M3', 4.9, 38),
                                                                                                                                                        ('华为Mate 60', '华为旗舰手机，支持卫星通信', 5999.00, 6999.00, 0.86, 2, 100, 200, '/images/mate60.jpg', '手机数码', '华为,手机,5G', 4.7, 89),
                                                                                                                                                        ('小米14', '骁龙8 Gen 3，徕卡影像', 3999.00, 4299.00, 0.93, 2, 80, 150, '/images/xiaomi14.jpg', '手机数码', '小米,手机,徕卡', 4.6, 67);

-- 插入时尚服饰
INSERT INTO products (name, description, price, original_price, discount, seller_id, stock, sales, image_url, category, tags, rating, review_count) VALUES
                                                                                                                                                        ('男士休闲夹克', '春秋季薄款夹克，舒适透气', 299.00, 399.00, 0.75, 3, 200, 300, '/images/jacket.jpg', '服装鞋帽', '男装,夹克,休闲', 4.5, 120),
                                                                                                                                                        ('女士连衣裙', '夏季新款碎花连衣裙', 199.00, 299.00, 0.67, 3, 150, 250, '/images/dress.jpg', '服装鞋帽', '女装,连衣裙,夏季', 4.6, 98),
                                                                                                                                                        ('运动鞋', '轻便透气运动鞋，适合跑步', 259.00, 359.00, 0.72, 3, 180, 400, '/images/shoes.jpg', '服装鞋帽', '运动鞋,跑步,轻便', 4.7, 156),
                                                                                                                                                        ('牛仔裤', '经典款牛仔裤，修身版型', 159.00, 199.00, 0.80, 3, 220, 350, '/images/jeans.jpg', '服装鞋帽', '牛仔裤,经典,修身', 4.4, 87);

-- 插入图书
INSERT INTO products (name, description, price, original_price, discount, seller_id, stock, sales, image_url, category, tags, rating, review_count) VALUES
                                                                                                                                                        ('Spring Boot实战', '深入讲解Spring Boot开发', 69.00, 89.00, 0.78, 4, 100, 150, '/images/springboot.jpg', '图书音像', '编程,Java,Spring', 4.8, 45),
                                                                                                                                                        ('Python编程', '从入门到实践', 59.00, 79.00, 0.75, 4, 120, 180, '/images/python.jpg', '图书音像', '编程,Python,入门', 4.7, 67),
                                                                                                                                                        ('三体全集', '刘慈欣科幻小说经典', 98.00, 128.00, 0.77, 4, 80, 200, '/images/santi.jpg', '图书音像', '科幻,小说,刘慈欣', 4.9, 89),
                                                                                                                                                        ('经济学原理', '曼昆经典经济学教材', 88.00, 108.00, 0.81, 4, 60, 90, '/images/economics.jpg', '图书音像', '经济学,教材,曼昆', 4.6, 34);

-- 插入购物车数据
INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES
                                                              (5, 1, 1),
                                                              (5, 5, 2),
                                                              (5, 9, 1),
                                                              (6, 2, 1),
                                                              (6, 6, 1),
                                                              (7, 3, 1),
                                                              (7, 7, 2),
                                                              (7, 11, 1);

-- 插入订单数据
INSERT INTO orders (order_number, user_id, total_amount, discount_amount, shipping_fee, actual_amount, status, shipping_address, receiver_name, receiver_phone, payment_method) VALUES
                                                                                                                                                                                    ('ORD202412010001', 5, 8398.00, 100.00, 0.00, 8298.00, 'COMPLETED', '北京市朝阳区建国路100号', '张三', '13500135001', '支付宝'),
                                                                                                                                                                                    ('ORD202412020001', 6, 13258.00, 200.00, 15.00, 13073.00, 'DELIVERED', '上海市浦东新区陆家嘴金融中心', '李四', '13500135002', '微信支付'),
                                                                                                                                                                                    ('ORD202412030001', 7, 6258.00, 50.00, 10.00, 6218.00, 'PAID', '广州市天河区珠江新城', '王五', '13500135003', '银行卡');

-- 插入订单项数据
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
                                                                    (1, 1, 1, 7999.00),
                                                                    (1, 5, 1, 299.00),
                                                                    (1, 9, 1, 69.00),
                                                                    (2, 2, 1, 12999.00),
                                                                    (2, 6, 1, 199.00),
                                                                    (2, 10, 1, 59.00),
                                                                    (3, 3, 1, 5999.00),
                                                                    (3, 7, 2, 259.00);