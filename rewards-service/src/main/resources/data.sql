-- Insert Users
INSERT INTO customers (id, customer_name) VALUES (101, 'Alice Smith');
INSERT INTO customers (id, customer_name) VALUES (102, 'Bob Jones');
INSERT INTO customers (id, customer_name) VALUES (103, 'Charlie Brown');
INSERT INTO customers (id, customer_name) VALUES (104, 'David Miller');
INSERT INTO customers (id, customer_name) VALUES (105, 'Emma Wilson');
INSERT INTO customers (id, customer_name) VALUES (106, 'John Smith');

-- Customer 101: Regular High Spender (Eligible for max points)
INSERT INTO purchases (id, customer_id, purchase_amount, purchase_date) VALUES (1, 101, 120.0, '2026-04-10');
INSERT INTO purchases (id, customer_id, purchase_amount, purchase_date) VALUES (2, 101, 50.0, '2026-05-15');

-- Customer 102: Mid-tier Spender (Qualifies mostly for the $50-$100 tier)
INSERT INTO purchases (id, customer_id, purchase_amount, purchase_date) VALUES (3, 102, 75.0, '2026-04-12');
INSERT INTO purchases (id, customer_id, purchase_amount, purchase_date) VALUES (4, 102, 90.0, '2026-06-18');

-- Customer 103: Mixed Activity (Some transactions qualify, others do not)
INSERT INTO purchases (id, customer_id, purchase_amount, purchase_date) VALUES (5, 103, 110.0, '2026-04-05');
INSERT INTO purchases (id, customer_id, purchase_amount, purchase_date) VALUES (6, 103, 75.0, '2026-05-22');

-- Customer 104: NOT ELIGIBLE (All transactions are under $50)
INSERT INTO purchases (id, customer_id, purchase_amount, purchase_date) VALUES (7, 104, 45.0, '2026-04-20');
INSERT INTO purchases (id, customer_id, purchase_amount, purchase_date) VALUES (8, 104, 20.0, '2026-05-11');

-- Customer 105: NOT ELIGIBLE (Exactly at the $50 baseline floor - yields 0 points)
INSERT INTO purchases (id, customer_id, purchase_amount, purchase_date) VALUES (9, 105, 50.0, '2026-06-01');

-- Customer 106: NOT ELIGIBLE (Transaction not in the Offer window)
INSERT INTO purchases (id, customer_id, purchase_amount, purchase_date) VALUES (10, 106, 250.0, '2026-01-01');