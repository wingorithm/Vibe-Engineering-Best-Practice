-- Seed Tiers
INSERT INTO customer_tier (name, discount_percentage) VALUES ('Beginner', 0.00);
INSERT INTO customer_tier (name, discount_percentage) VALUES ('Fans', 10.00);
INSERT INTO customer_tier (name, discount_percentage) VALUES ('Lovers', 30.00);

-- Seed Mock Customers
INSERT INTO customer (id, name, email, tier_id) 
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Alice Lover', 'alice@example.com', 3);
INSERT INTO customer (id, name, email, tier_id) 
VALUES ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Bob Fan', 'bob@example.com', 2);
INSERT INTO customer (id, name, email, tier_id) 
VALUES ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Charlie Beginner', 'charlie@example.com', 1);
