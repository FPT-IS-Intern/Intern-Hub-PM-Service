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
- Base path public API: `/pm`
- `GET /pm/projects`: danh sách dự án.
- `POST /pm/projects` (multipart): tạo dự án + tài liệu charter.
- `GET /pm/projects/{projectId}`: chi tiết dự án.
- `PUT /pm/projects/{projectId}` (multipart): cập nhật dự án + thay bộ tài liệu charter nếu có file mới.
- `DELETE /pm/projects/{projectId}`: hủy mềm dự án.
- `POST /pm/projects/{projectId}/users`: thêm thành viên vào dự án.
- `GET /pm/projects/{projectId}/users`: danh sách thành viên dự án.
- `PUT /pm/projects/users/{memberId}`: sửa vai trò thành viên.
- `DELETE /pm/projects/users/{memberId}`: xóa mềm thành viên khỏi dự án.
- `POST /pm/projects/{projectId}/tasks` (multipart): tạo task + tài liệu hướng dẫn.
- `GET /pm/projects/{projectId}/tasks`: danh sách task theo dự án.
- `GET /pm/tasks/{taskId}`: chi tiết task.
- `PUT /pm/tasks/{taskId}` (multipart): cập nhật task + thay bộ tài liệu hướng dẫn nếu có file mới.
- `DELETE /pm/tasks/{taskId}`: hủy mềm task.
- `POST /pm/tasks/{taskId}/submit` (multipart): nộp kết quả task + thay bộ file submission cũ bằng bộ mới nhất.
- `POST /pm/tasks/{taskId}/approve`: duyệt task.
- `POST /pm/tasks/{taskId}/refuse`: yêu cầu làm lại task.
- `GET /pm/my-tasks`: danh sách task của user hiện tại.
- `POST /pm/projects/{projectId}/extend`: gia hạn dự án.
- `POST /pm/projects/{projectId}/complete`: kết thúc dự án.

**Xác thực và phân quyền**
- Public API dùng `Bearer JWT`.
- Internal API dùng header `X-Internal-Secret` qua security starter và Feign configuration.
- Chỉ chủ dự án mới được sửa, hủy, gia hạn, hoàn thành dự án và quản lý thành viên dự án.
- Chỉ người tạo task mới được sửa, hủy, duyệt hoặc yêu cầu làm lại task.
- Chỉ người được giao task mới được nộp kết quả task.

**Cách gửi multipart**
- Với `POST/PUT /pm/projects...` và `POST/PUT /pm/tasks...`, body là `multipart/form-data`.
- Part `request`: JSON chứa dữ liệu nghiệp vụ.
- Part `files`: danh sách file tài liệu, có thể bỏ trống.
- Với `POST /pm/tasks/{taskId}/submit`:
  - field `deliverableLink`: link kết quả, không bắt buộc
  - field `files`: danh sách file submission, không bắt buộc

**Ví dụ request JSON**
- Project `request`:
```json
{
  "name": "Du an A",
  "description": "Mo ta du an",
  "note": "Ghi chu",
  "status": "NOT_STARTED",
  "budgetToken": 1000,
  "rewardToken": 200,
  "assigneeId": 123,
  "deliverableDescription": "Tai lieu ban giao",
  "deliverableLink": "https://example.com",
  "endAt": 1770000000000
}
```
- Task `request`:
```json
{
  "name": "Task 1",
  "description": "Mo ta task",
  "note": "Ghi chu",
  "status": "NOT_STARTED",
  "rewardToken": 100,
  "assigneeId": 123,
  "deliverableDescription": "Ket qua can nop",
  "deliverableLink": "https://example.com"
}
```
- Approve/refuse:
```json
{
  "reviewComment": "Can bo sung bang chung",
  "recoveredToken": 50
}
```
- Extend project:
```json
{
  "endAt": 1775000000000,
  "reason": "Can them thoi gian"
}
```
- Complete project:
```json
{
  "completionComment": "Hoan thanh dung ke hoach",
  "recoveredToken": 50,
  "bonusToken": 100
}
```

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
