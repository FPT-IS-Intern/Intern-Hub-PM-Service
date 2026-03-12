# Intern Hub PM Service

Service PM trong hệ sinh thái Intern Hub, quản lý dự án, nhiệm vụ, thành viên dự án và bài nộp công việc.

**Phạm vi nghiệp vụ**
- Thuộc service `intern-hub-pm-service`, kiến trúc single-module Maven.
- Tích hợp liên service qua `Intern-Hub-Security-Starter`, HRM internal và DMS internal.

**Nghiệp vụ chính**
1. Quản trị dự án.
Chi tiết: tạo/cập nhật/hủy dự án; gắn PM phụ trách, ngân sách điểm, điểm thưởng, thành viên tham gia và tài liệu dự án.
2. Quản trị nhiệm vụ trong dự án.
Chi tiết: tạo/cập nhật/hủy task trực tiếp dưới project; chỉ định người thực hiện, ngân sách điểm, điểm thưởng và tài liệu hướng dẫn.
3. Nộp kết quả nhiệm vụ.
Chi tiết: người được giao nộp kết quả và file minh chứng; hệ thống thay thế bộ file nộp cũ bằng bộ mới nhất.
4. Duyệt nhiệm vụ.
Chi tiết: người giao việc có thể yêu cầu sửa (`NEEDS_REVISION`) hoặc duyệt hoàn thành (`COMPLETED`), đồng thời ghi nhận điểm thu hồi và nhận xét.
5. Gia hạn và kết thúc dự án.
Chi tiết: gia hạn ngày kết thúc với lý do; kết thúc dự án chỉ khi không còn task `PENDING_REVIEW`, đồng thời ghi nhận điểm thu hồi và thưởng thêm.
6. Tra cứu công việc của tôi.
Chi tiết: lấy danh sách task được giao cho user hiện tại qua `my-tasks`.

**Quy tắc nghiệp vụ & ràng buộc**
- Mô hình hiện tại chỉ còn `PROJECT` và `TASK`; không còn `MODULE`.
- Trạng thái công việc:
  - `NOT_STARTED`
  - `IN_PROGRESS`
  - `OVERDUE`
  - `NEEDS_REVISION`
  - `PENDING_REVIEW`
  - `COMPLETED`
  - `CANCELED`
- Chỉ chủ dự án mới được sửa/hủy dự án.
- Chỉ người giao task mới được sửa/hủy/duyệt task.
- Task chỉ được duyệt khi đang ở trạng thái `PENDING_REVIEW`.
- Dự án chỉ được kết thúc khi không còn task nào ở trạng thái `PENDING_REVIEW`.
- File dự án, file hướng dẫn task và file submission đều upload qua DMS.
- User nội bộ không lưu tại PM; mọi tra cứu user đi qua HRM internal.

**Dữ liệu chính**
- WorkItem: lưu cả project và task, gồm trạng thái, ngân sách điểm, điểm thưởng, điểm thu hồi, điểm thưởng thêm, thời gian.
- EntityMember: thành viên tham gia dự án và vai trò trong dự án.
- Document: metadata file gắn với project/task/submission, object key lưu trên DMS.

**API nghiệp vụ (tóm tắt)**
- `GET /projects`: danh sách dự án.
- `POST /projects` (multipart): tạo dự án + tài liệu.
- `GET /projects/{projectId}`: chi tiết dự án.
- `PUT /projects/{projectId}` (multipart): cập nhật dự án + tài liệu.
- `DELETE /projects/{projectId}`: hủy dự án.
- `POST /projects/{projectId}/users`: thêm thành viên vào dự án.
- `GET /projects/{projectId}/users`: danh sách thành viên dự án.
- `PUT /projects/users/{memberId}`: sửa vai trò thành viên.
- `DELETE /projects/users/{memberId}`: xóa thành viên khỏi dự án.
- `POST /projects/{projectId}/tasks` (multipart): tạo task + tài liệu hướng dẫn.
- `GET /projects/{projectId}/tasks`: danh sách task theo dự án.
- `GET /tasks/{taskId}`: chi tiết task.
- `PUT /tasks/{taskId}` (multipart): cập nhật task + tài liệu hướng dẫn.
- `DELETE /tasks/{taskId}`: hủy task.
- `POST /tasks/{taskId}/submit` (multipart): nộp kết quả task.
- `POST /tasks/{taskId}/approve`: duyệt task.
- `POST /tasks/{taskId}/refuse`: yêu cầu làm lại task.
- `GET /my-tasks`: danh sách task của user hiện tại.
- `POST /projects/{projectId}/extend`: gia hạn dự án.
- `POST /projects/{projectId}/complete`: kết thúc dự án.

**Cấu hình liên quan nghiệp vụ**
- DB schema: `ih_pm`.
- File upload: `50MB` cho `multipart`.
- DMS:
  - `services.dms.url`
  - `services.dms.system-actor-id`
- HRM:
  - `services.hrm.url`
- Gateway cho Swagger:
  - `gateway-url` (mặc định `http://localhost:8765`)
