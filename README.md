# LKMS: Lab Knowledge Management System

Một ứng dụng Android toàn diện được xây dựng bằng Java, được thiết kế để số hóa và quản lý 18 nghiệp vụ cốt lõi trong phòng thí nghiệm. Dự án này thay thế sổ tay giấy và các quy trình rời rạc bằng một nền tảng di động, tập trung, được hỗ trợ bởi Supabase.

## 📜 Mô tả

Trong các phòng thí nghiệm R\&D truyền thống, việc quản lý phụ thuộc vào sổ ghi chép, file Excel và email. Điều này dẫn đến mất mát dữ liệu, khó khăn trong việc tra cứu, không có khả năng kiểm soát phiên bản (version control) cho các quy trình, và xung đột khi đặt lịch thiết bị.

**LKMS (Lab Knowledge Management System)** là giải pháp "all-in-one" trên nền tảng Android, cung cấp một nguồn dữ liệu trung tâm (single source of truth) cho mọi hoạt động của lab. Ứng dụng này được xây dựng theo kiến trúc 3 tầng (MVVM + Use Case + Repository) để đảm bảo tính module hóa, dễ bảo trì và dễ kiểm thử.

## ✨ Tính năng Cốt lõi (Triển khai 18 Use Cases)

Dự án triển khai đầy đủ 18 nghiệp vụ, được chia thành 4 nhóm chính:

### 1\. 🔐 Quản lý Truy cập & Người dùng (UC 1, 14)

  * **Đăng nhập & Phân quyền:** Hệ thống đăng nhập dựa trên vai trò (Lab Manager, Researcher, Technician).
  * **Bảo mật:**
      * Mật khẩu người dùng được hash bằng **BCrypt** trước khi lưu vào CSDL. Logic xác thực sử dụng `BCrypt.checkpw`.
      * Phiên đăng nhập được duy trì bằng **JWT (JSON Web Tokens)**.
      * Token, ID và vai trò của người dùng được lưu trữ an toàn trên thiết bị bằng **EncryptedSharedPreferences**.
  * **Quản lý Hồ sơ:** Người dùng có thể xem và chỉnh sửa thông tin cá nhân.
  * **Quản lý Nhóm:** Lab Manager có thể xem danh sách thành viên và thay đổi vai trò của họ.

### 2\. 🔬 Quy trình & Thí nghiệm (UC 3, 4, 5, 6, 12, 15)

  * **Thư viện Protocol (SOP):** Tra cứu và lọc các quy trình thí nghiệm.
  * **Chi tiết Protocol:** Xem chi tiết từng bước, vật tư và cảnh báo an toàn.
  * **Tạo Thí nghiệm:** Tạo một thí nghiệm mới (Experiment) từ một Protocol có sẵn.
  * **Sổ tay Lab Điện tử (ELN):** Giao diện phức tạp hiển thị timeline thí nghiệm, sử dụng `RecyclerView` với nhiều `ViewType` để lồng ghép các bước (Steps) và các nhật ký (Logs).
  * **Ghi Log (UC12):** Thêm ghi chú văn bản, hình ảnh, hoặc đính kèm file vào từng bước thí nghiệm.
  * **Xuất Báo cáo (UC15):** Tạo và tải báo cáo PDF tóm tắt toàn bộ thí nghiệm.

### 3\. 📦 Quản lý Tài nguyên (UC 7, 8, 9, 10, 11)

  * **Đặt lịch Thiết bị (UC9):** Đặt lịch sử dụng thiết bị với giao diện `MaterialCalendarView`. Các ngày đã được đặt sẽ bị vô hiệu hóa.
  * **Kiểm tra Xung đột:** Logic nghiệp vụ được xử lý ở tầng Domain (`EquipmentBookingUseCase`) để kiểm tra xung đột lịch ngay trên client trước khi gửi yêu cầu.
  * **Chi tiết Thiết bị (UC10):** Xem thông tin model, serial, và lịch sử bảo trì (`MaintenanceLogFragment`).
  * **Tài liệu (Manuals):** Xem tài liệu hướng dẫn PDF của thiết bị ngay trong ứng dụng thông qua `WebView`.
  * **Quản lý Tồn kho (UC7, 8):** Quản lý (thêm/sửa/check-in/check-out) hóa chất và vật tư tiêu hao.
  * **Tra cứu SDS (UC11):** Tra cứu Bảng dữ liệu An toàn Hóa chất (SDS) theo Tên hoặc CAS Number.
      * *Kỹ thuật:* Sử dụng câu lệnh `OR` của Supabase API (`?or=(itemName.like.*query*,casNumber.like.*query*)`) để tìm kiếm trên nhiều cột.

### 4\. 🚀 Quản lý Dự án & Cộng tác (UC 16, 17, 18)

  * **Quản lý Dự án (UC18):** Tạo và quản lý các dự án, liên kết các thành viên và thí nghiệm vào chung một dự án.
  * **Giao diện Tab (Tabs):** Màn hình chi tiết dự án sử dụng `ViewPager2` và `TabLayout` để hiển thị các khía cạnh khác nhau của dự án (Thông tin chung, Thí nghiệm, Peer Review).
  * **Xử lý "Join" phía Client:** Logic "lấy các dự án mà user tham gia" được xử lý ở tầng Domain (`ProjectUserCase`) bằng cách lấy 2 danh sách (Project và Team) và thực hiện join thủ công trên client.
  * **Peer Review (UC16):** Tính năng cho phép tạo và xem lịch các buổi bình duyệt khoa học, được tích hợp trực tiếp vào tab của dự án.

-----

## 🏗️ Kiến trúc Phần mềm

Dự án được xây dựng theo kiến trúc 3 tầng (MVVM + Use Cases), lấy cảm hứng từ Clean Architecture, để đảm bảo tính **Separation of Concerns (SoC)**.

1.  **UI Layer (`com.lkms.ui.*`)**

      * **Thành phần:** `Activities`, `Fragments`, `Adapters`, và `ViewModels`.
      * **Trách nhiệm:** Chỉ chịu trách nhiệm hiển thị dữ liệu lên màn hình và gửi các sự kiện (ví dụ: `onClick`) tới `ViewModel`.
      * `ViewModel` (ví dụ: `ProtocolListViewModel`) giữ trạng thái cho UI (sử dụng `LiveData`) và gọi các `UseCase` để thực hiện hành động.

2.  **Domain Layer (`com.lkms.domain.*`)**

      * **Thành phần:** `UseCases` (ví dụ: `GetLatestApprovedProtocolsUseCase`).
      * **Trách nhiệm:** Đây là "bộ não" của ứng dụng, chứa toàn bộ logic nghiệp vụ (business logic). Ví dụ: `EquipmentBookingUseCase` chứa logic kiểm tra xung đột lịch.
      * Tầng này là **pure Java**, không phụ thuộc vào bất kỳ thư viện Android nào.

3.  **Data Layer (`com.lkms.data.*`)**

      * **Thành phần:** `Repositories` (Interface và Implementation), `Models` (POJO), và `HttpHelper`.
      * **Trách nhiệm:** Quản lý tất cả các nguồn dữ liệu (trong trường hợp này là Supabase).
      * Sử dụng **Repository Pattern**: Tầng Domain chỉ "biết" đến các Interface (ví dụ: `IProtocolRepository`), trong khi `ProtocolRepositoryImplJava` cung cấp cách triển khai cụ thể (gọi API Supabase bằng `HttpHelper`).

-----

## 🛠️ Công nghệ & Thư viện

  * **Ngôn ngữ:** Java
  * **Kiến trúc:** MVVM + Use Cases + Repository
  * **Backend:** [Supabase](https://supabase.com/) (Backend-as-a-Service)
  * **API & Networking:** `java.net.HttpURLConnection` (bên trong lớp `HttpHelper`)
  * **JSON Parsing:** `com.google.code.gson:gson`
  * **Bảo mật:**
      * `org.mindrot:jbcrypt` (Hashing mật khẩu)
      * `com.auth0:java-jwt` (Tạo và xác thực Token)
      * `androidx.security:security-crypto` (Lưu trữ token bằng `EncryptedSharedPreferences`)
  * **UI Components:**
      * `com.google.android.material:material` (Gồm `TabLayout`, `FloatingActionButton`, v.v.)
      * `androidx.viewpager2:viewpager2` (Quản lý các tab trong Project Detail)
      * `com.applandeo:material-calendar-view` (Hiển thị lịch đặt thiết bị)
      * `com.journeyapps:zxing-android-embedded` (Quét QR/Barcode)
  * **Testing:**
      * Android Instrumented Tests (JUnit 4) để kiểm thử tầng Repository (ví dụ: `ProtocolRepositoryImplTest.java`, `AuthRepositoryImplJavaTest.java`).

-----

## 🚀 Bắt đầu (Getting Started)

Để build và chạy dự án này, bạn sẽ cần:

1.  **Clone repository:**

    ```sh
    git clone [URL_CUA_BAN]
    cd PRM_FinalProject-Sprint-4-Push-all-of-your-last-code-to-here-after-review-done-
    ```

2.  **Mở bằng Android Studio:** Mở dự án bằng Android Studio (phiên bản Flamingo trở lên).

3.  **Cấu hình Biến Môi trường (QUAN TRỌNG):**
    Dự án này cần khóa API của Supabase để hoạt động. Hãy tạo một file tên là `local.properties` trong thư mục gốc của dự án (cùng cấp với `settings.gradle.kts`).
    *Nội dung file `local.properties`:*

    ```properties
    SUPABASE_URL="YOUR_SUPABASE_PROJECT_URL"
    SUPABASE_ANON_KEY="YOUR_SUPABASE_ANON_KEY"
    JWT_SECRET="YOUR_JWT_SECRET_KEY_DUNG_DE_TAO_TOKEN"
    ```

    (Các giá trị này được đọc bởi file `app/build.gradle.kts`).

4.  **Build & Run:**
    Đồng bộ Gradle (Sync Gradle) và chạy ứng dụng trên máy ảo hoặc thiết bị Android (yêu cầu API 24+).

-----

## 🧑‍💻 Đội ngũ Phát triển

Dự án này là nỗ lực chung của 5 thành viên, với trách nhiệm được phân chia theo các nhóm module/use case.

  * **Đặng Thanh Hải - thanhhaidangabc@gmail.com:**
      * Equipment Booking (UC9)
      * Equipment Details & Manuals (UC10)
      * SDS Lookup (UC11)
      * Project Management (UC18)
      * Peer Review (UC16)
  * **Trần Thị Ngọc Ánh:**
      * Authentication & Security (UC1)
      * Inventory Management (UC7, UC8)
      * Main dashboard
  * **Lê Đức Việt:**
      * Protocol/SOP Management (UC3, UC4)
      * Create Experiment
  * **Lê Huy Điệp:**
      * Experiment Logbook (UC6)
      * File/Data Upload (UC12)
      * Reporting (UC15)
      * Team Collaboration & Comments (UC13, UC17)
  * **Đỗ Ngọc Hoàng Anh - da.flying.castle@gmail.com:**
      * Add/Update inventory
      * Checkin/Checkout
      * User Profile
      * Role Management

     
-----
