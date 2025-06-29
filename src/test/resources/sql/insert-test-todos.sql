DELETE FROM todos WHERE 1=1;
INSERT INTO todos (id, title, description, completed, created_at, updated_at) VALUES
(1,'Todo 1', 'Desc 1', false, NOW(), NOW()),
(2, 'Todo 2', 'Desc 2', true, NOW(), NOW()),
(3, 'Todo 3', 'Desc 3', false, NOW(), NOW());