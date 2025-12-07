<template>
	<div class="markdown-copy-buttons">
		<div class="markdown-copy-buttons-inner">
			<button ref="copyBtn" class="copy" @click="copyMarkdown">
				<span v-html="copied ? iconCheck : iconCopy"></span> 复制 Markdown
			</button>
			<button ref="downloadBtn" class="download" @click="downloadMarkdown">
				<span v-html="downloaded ? iconCheck : iconDownload"></span> 下载 Markdown
			</button>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

//#region Utils
function removeHtmlExtension(pathSegment: string): string {
	const lastSlashIndex = pathSegment.lastIndexOf("/")
	const lastDotIndex = pathSegment.lastIndexOf(".")
	if (lastDotIndex > lastSlashIndex && lastDotIndex !== -1 && pathSegment.endsWith(".html")) {
		return pathSegment.substring(0, lastDotIndex)
	}
	return pathSegment
}

function cleanUrl(url: string): string {
	const { origin, pathname } = new URL(url)
	const pathnameWithoutTrailingSlash = pathname.replace(/\/+$/, "")
	if (pathname.length) {
		return origin + removeHtmlExtension(pathnameWithoutTrailingSlash)
	} else {
		return origin
	}
}

function resolveMarkdownPageURL(url: string): string {
	const cleanedURL = cleanUrl(url)
	if (cleanedURL === window.location.origin) {
		return `${cleanedURL}/index.md`
	} else {
		return `${cleanedURL}.md`
	}
}

function downloadFile(filename: string, content: string, blobType = "text/plain"): void {
	const blob = new Blob([content], { type: blobType })
	const url = URL.createObjectURL(blob)
	const link = document.createElement("a")
	link.href = url
	link.download = filename
	link.click()
	URL.revokeObjectURL(url)
}
//#endregion

//#region SVG Icons
const iconCheck =
	'<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-check-icon lucide-check"><path d="M20 6 9 17l-5-5"/></svg>'
const iconCopy =
	'<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-copy-icon lucide-copy"><rect width="14" height="14" x="8" y="8" rx="2" ry="2"/><path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"/></svg>'
const iconDownload =
	'<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-download-icon lucide-download"><path d="M12 15V3"/><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><path d="m7 10 5 5 5-5"/></svg>'
//#endregion

const copied = ref(false)
const downloaded = ref(false)

const currentURL = window.location.origin + window.location.pathname

/** Copies markdown content from the current page to clipboard */
function copyMarkdown() {
	fetch(resolveMarkdownPageURL(currentURL))
		.then((response) => response.text())
		.then((text) => navigator.clipboard.writeText(text))
		.then(() => {
			copied.value = true
			setTimeout(() => {
				copied.value = false
			}, 2000)
		})
		.catch((error) => console.error('❌ Error copying markdown:', error))
}

/** Downloads markdown content from the current page as a file */
function downloadMarkdown() {
	const markdownPageUrl = resolveMarkdownPageURL(currentURL)
	fetch(markdownPageUrl)
		.then((response) => response.text())
		.then((text) => {
			const filename = markdownPageUrl.split('/').pop() || 'page.md'
			downloadFile(filename, text, 'text/markdown')

			downloaded.value = true
			setTimeout(() => {
				downloaded.value = false
			}, 2000)
		})
		.catch((error) => console.error('❌ Error downloading markdown:', error))
}
</script>

<style scoped>
.markdown-copy-buttons {
	width: 100%;
	display: flex;
	justify-content: flex-end;
	align-items: center;
	margin-top: -16px;
	margin-bottom: -32px;
}
.markdown-copy-buttons-inner {
	display: flex;
	gap: 12px;
}
.markdown-copy-buttons button {
	padding: 4px 12px;
	border-radius: 6px;
	font-size: 13px;
	font-weight: 500;
	cursor: pointer;
	display: flex;
	align-items: center;
	gap: 6px;
	transition: all 0.2s ease;
	background: transparent;
	border: none;
	color: var(--vp-c-text-2);
	user-select: none;
}
.markdown-copy-buttons button:hover {
	background: var(--vp-c-bg-soft);
	color: var(--vp-c-brand);
}
.markdown-copy-buttons button:active {
	transform: scale(0.98);
}
.markdown-copy-buttons img {
	vertical-align: middle;
}

@media (max-width: 640px) {
	.markdown-copy-buttons button {
		padding: 3px 10px;
		font-size: 12px;
		gap: 4px;
	}
	
	.markdown-copy-buttons button span:last-child {
		display: none;
	}
}
</style>

