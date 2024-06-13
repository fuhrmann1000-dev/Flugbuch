<?php
// Report all PHP errors
error_reporting(E_ALL);

require_once ('./include/File.php');
require_once ('./include/FieldStatus.php');
require_once ('./include/FileResult.php');
require_once ('./include/FileService.php');
require_once ('./include/FlugImportService.php');

$fileService = new FileService();
$flugImportService = new FlugImportService();

if (isset($_POST['import'])) {
	$file = new File();
	$fileResult = $fileService->uploadFile();

	$file = $fileResult->file;
	$flugImportService->importData($file->fileName);
	$flugImportService->pullImportedData();
} else {
	//echo 'not save';
}

//print_r($fileResult);

?>
<!doctype html>
<html lang="de">

<head>
	<!-- Required meta tags -->
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<!--favicon-->
	<link rel="icon" href="assets/images/favicon-32x32.png" type="image/png" />
	<!--plugins-->
	<link href="assets/plugins/simplebar/css/simplebar.css" rel="stylesheet" />
	<link href="assets/plugins/perfect-scrollbar/css/perfect-scrollbar.css" rel="stylesheet" />
	<link href="assets/plugins/metismenu/css/metisMenu.min.css" rel="stylesheet" />
	<link rel="stylesheet" href="assets/plugins/notifications/css/lobibox.min.css" />
	<!-- loader-->
	<link href="assets/css/pace.min.css" rel="stylesheet" />
	<script src="assets/js/pace.min.js"></script>
	<!-- Bootstrap CSS -->
	<link href="assets/css/bootstrap.min.css" rel="stylesheet">
	<link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500&display=swap" rel="stylesheet">
	<link href="assets/css/app.css" rel="stylesheet">
	<link href="assets/css/icons.css" rel="stylesheet">

	<title>Flugbuch</title>
</head>

<body class="bg-theme bg-theme1">
	<!--wrapper-->
	<div class="wrapper">
		<!--sidebar wrapper -->
		<div class="sidebar-wrapper" data-simplebar="true">
			<div class="sidebar-header">
				<div>
					<img src="assets/images/logo-icon.png" class="logo-icon" alt="logo icon">
				</div>
				<div>
					<h4 class="logo-text">Hauptflugbuch</h4>
				</div>

			</div>
			<!--navigation-->
			<?php include ('inc/navigation.php') ?>
			<!--end navigation-->
		</div>
		<!--end sidebar wrapper -->
		<!--start header -->
		<?php include ('inc/header.php') ?>
		<!--end header -->
		<!--start page wrapper -->
		<div class="page-wrapper">
			<div class="page-content">
				<!--breadcrumb-->
				<div class="page-breadcrumb d-none d-sm-flex align-items-center mb-3">
					<div class="breadcrumb-title pe-3">Flugbuch</div>
					<div class="ps-3">
						<nav aria-label="breadcrumb">
							<ol class="breadcrumb mb-0 p-0">
								<li class="breadcrumb-item"><a href="javascript:;"><i class="bx bx-home-alt"></i></a>
								</li>
								<li class="breadcrumb-item active" aria-current="page">Flugdaten importieren</li>
							</ol>
						</nav>
					</div>
					<!--<div class="ms-auto">
						<div class="btn-group">
							<button type="button" class="btn btn-light">Settings</button>
							<button type="button" class="btn btn-light dropdown-toggle dropdown-toggle-split"
								data-bs-toggle="dropdown"> <span class="visually-hidden">Toggle Dropdown</span>
							</button>
							<div class="dropdown-menu dropdown-menu-right dropdown-menu-lg-end"> <a
									class="dropdown-item" href="javascript:;">Action</a>
								<a class="dropdown-item" href="javascript:;">Another action</a>
								<a class="dropdown-item" href="javascript:;">Something else here</a>
								<div class="dropdown-divider"></div> <a class="dropdown-item"
									href="javascript:;">Separated link</a>
							</div>
						</div>
					</div>-->
				</div>
				<!--end breadcrumb-->
				<div class="row">
					<div class="col-xl-12 mx-auto">
						<h6 class="mb-0 text-uppercase">Flugdaten importieren</h6>
						<hr />
						<div class="card border-top border-0 border-4 border-white">
							<div class="card-body p-5">
								<div class="card-title d-flex align-items-center">
									<div><i class="bx bxs-user me-1 font-22 text-white"></i>
									</div>
									<h5 class="mb-0 text-white">Flugdaten importieren</h5>
								</div>
								<hr>
								<form class="row g-3" method="post" enctype="multipart/form-data">
									<div class="col-md-12">
										<label for="<?= File::FILE_NAME ?>" class="form-label">CSV-Datei</label>
										<input type="file" id="<?= File::FILE_NAME ?>" name="<?= File::FILE_NAME ?>"
											class="form-control" title="Bitte gib die Datei für den Import an." />
										<?php
										if (isset($fileResult)) {
											$fieldStatusFile = $fileResult->fieldStatusList[File::FILE_NAME];
											if (isset($fieldStatusFile)) {
												if (FieldStatus::INFO == $fieldStatusFile->messageType) {
													?>
													<p style="color: green;">
														<?= $fieldStatusFile->message ?>
													</p>
													<?php
												}
												if (FieldStatus::ERROR == $fieldStatusFile->messageType) {
													?>
													<p style="color: red;">
														<?= $fieldStatusFile->message ?>
													</p>
													<?php
												}
											}
										}
										?>

									</div>

									<div class="col-12">
										<button type="submit" class="btn btn-light px-5"
											name="import">Speichern</button>
									</div>
								</form>
							</div>
						</div>

					</div>
				</div>
				<!--end row-->

			</div>
		</div>
		<!--end page wrapper -->
		<!--start overlay-->
		<div class="overlay toggle-icon"></div>
		<!--end overlay-->
		<!--Start Back To Top Button--> <a href="javaScript:;" class="back-to-top"><i
				class='bx bxs-up-arrow-alt'></i></a>
		<!--End Back To Top Button-->
		<footer class="page-footer">
			<p class="mb-0"></p>
		</footer>
	</div>
	<!--end wrapper-->

	<!-- Bootstrap JS -->
	<script src="assets/js/bootstrap.bundle.min.js"></script>
	<!--plugins-->
	<script src="assets/js/jquery.min.js"></script>
	<script src="assets/plugins/simplebar/js/simplebar.min.js"></script>
	<script src="assets/plugins/metismenu/js/metisMenu.min.js"></script>
	<script src="assets/plugins/perfect-scrollbar/js/perfect-scrollbar.js"></script>
	<!--app JS-->
	<script src="assets/js/app.js"></script>

	<!--calendar-->
	<link rel="stylesheet" href="//code.jquery.com/ui/1.13.2/themes/base/jquery-ui.css">
	<script src="https://code.jquery.com/ui/1.13.2/jquery-ui.js"></script>
	<script src="assets/i18n/datepicker-de.js"></script>
	<script>

	</script>

	<script>

	</script>
	<!--notification js -->
	<script src="assets/plugins/notifications/js/lobibox.min.js"></script>
	<script src="assets/plugins/notifications/js/notifications.min.js"></script>
	<script>
		$(document).ready(function () {

			<?php
			if (FileResult::INFO == $fileResult->resultMessageType) {
				?>
				Lobibox.notify('success', {
					pauseDelayOnHover: true,
					continueDelayOnInactiveTab: false,
					position: 'top right',
					icon: 'bx bx-info-circle',
					title: 'Information',
						msg: '<?= $fileResult->resultMessage ?>'
					});
			<?php
			}
			?>
		<?php
		if (FileResult::WARNING == $fileResult->resultMessageType) {
			?>
				Lobibox.notify('warning', {
					pauseDelayOnHover: true,
						continueDelayOnInactiveTab: false,
						position: 'top right',
					icon: 'bx bx-info-circle',
					title: 'Achtung',
					msg: '<?= $fileResult->resultMessage ?>'
				});
			<?php
		}
		?>
		<?php
		if (FileResult::ERROR == $fileResult->resultMessageType) {
			?>
					Lobibox.notify('error', {
						pauseDelayOnHover: true,
						continueDelayOnInactiveTab: false,
						position: 'top right',
						icon: 'bx bx-info-circle',
						title: 'Es ist ein Fehler aufgetreten',
						msg: '<?= $fileResult->resultMessage ?>'
					});
				<?php
		}
		?>

		});
	</script>
</body>

</html>