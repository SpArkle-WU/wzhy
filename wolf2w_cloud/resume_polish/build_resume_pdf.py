from pathlib import Path
import html
import re

from docx import Document
from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT, TA_RIGHT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    KeepTogether,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)


ROOT = Path(__file__).resolve().parent
SOURCE = ROOT / "resume_original.docx"
OUTPUT = ROOT.parent / "output" / "pdf" / "吴志鸿_Java_Agent开发_简历.pdf"

NAVY = colors.HexColor("#17324D")
TEAL = colors.HexColor("#137C75")
INK = colors.HexColor("#25323D")
MUTED = colors.HexColor("#667681")
LIGHT = colors.HexColor("#EDF3F5")
RULE = colors.HexColor("#CAD6DB")
WHITE = colors.white


def normalize(text):
    return (
        text.replace("\u00a0", " ")
        .replace("\t", " ")
        .replace("——", "-")
        .replace("—", "-")
        .replace("–", "-")
        .strip()
    )


def esc(text):
    return html.escape(normalize(text))


def split_label(text):
    match = re.match(r"^([^:：]+[:：])(.*)$", normalize(text))
    return (match.group(1), match.group(2).strip()) if match else ("", normalize(text))


def register_fonts():
    pdfmetrics.registerFont(TTFont("ResumeCN", r"C:\Windows\Fonts\msyh.ttc", subfontIndex=0))
    pdfmetrics.registerFont(TTFont("ResumeCN-Bold", r"C:\Windows\Fonts\msyhbd.ttc", subfontIndex=0))
    pdfmetrics.registerFontFamily(
        "ResumeCN",
        normal="ResumeCN",
        bold="ResumeCN-Bold",
        italic="ResumeCN",
        boldItalic="ResumeCN-Bold",
    )


def footer(canvas, doc):
    canvas.saveState()
    width, _ = A4
    y = 9.5 * mm
    canvas.setStrokeColor(RULE)
    canvas.setLineWidth(0.45)
    canvas.line(doc.leftMargin, y + 4.5 * mm, width - doc.rightMargin, y + 4.5 * mm)
    canvas.setFont("ResumeCN", 7.2)
    canvas.setFillColor(MUTED)
    canvas.drawRightString(
        width - doc.rightMargin,
        y,
        f"吴志鸿  |  Java / Agent 应用开发  |  {canvas.getPageNumber()}",
    )
    canvas.restoreState()


def build_styles():
    styles = getSampleStyleSheet()
    body = ParagraphStyle(
        "ResumeBody",
        parent=styles["BodyText"],
        fontName="ResumeCN",
        fontSize=9.0,
        leading=11.8,
        textColor=INK,
        spaceBefore=0,
        spaceAfter=2.0,
        wordWrap="CJK",
        allowWidows=0,
        allowOrphans=0,
    )
    bullet = ParagraphStyle(
        "ResumeBullet",
        parent=body,
        fontSize=8.75,
        leading=11.4,
        leftIndent=10.5,
        firstLineIndent=-10.5,
        bulletIndent=0,
        spaceAfter=1.8,
    )
    label = ParagraphStyle(
        "ResumeLabel",
        parent=body,
        fontSize=9.0,
        leading=11.8,
        spaceAfter=2.1,
    )
    section = ParagraphStyle(
        "ResumeSection",
        parent=body,
        fontName="ResumeCN-Bold",
        fontSize=11.8,
        leading=14,
        textColor=NAVY,
        leftIndent=5,
        spaceBefore=6.2,
        spaceAfter=3.8,
        keepWithNext=True,
    )
    project = ParagraphStyle(
        "ResumeProject",
        parent=body,
        fontName="ResumeCN-Bold",
        fontSize=9.8,
        leading=12.3,
        textColor=NAVY,
        spaceBefore=1.8,
        spaceAfter=1.7,
        keepWithNext=True,
    )
    return body, bullet, label, section, project


def section_heading(title, section_style):
    title_p = Paragraph(esc(title), section_style)
    table = Table([[title_p]], colWidths=[181 * mm])
    table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, -1), WHITE),
                ("LINEBEFORE", (0, 0), (0, 0), 2.2, TEAL),
                ("LINEBELOW", (0, 0), (0, 0), 0.45, RULE),
                ("LEFTPADDING", (0, 0), (0, 0), 0),
                ("RIGHTPADDING", (0, 0), (0, 0), 0),
                ("TOPPADDING", (0, 0), (0, 0), 0),
                ("BOTTOMPADDING", (0, 0), (0, 0), 0),
            ]
        )
    )
    return table


def bullet(text, bullet_style):
    clean = re.sub(r"^[•·\s]+", "", normalize(text))
    return Paragraph(f"•&nbsp;{esc(clean)}", bullet_style)


def label_value(label, value, label_style):
    return Paragraph(
        f'<font name="ResumeCN-Bold" color="#137C75">{esc(label)}</font>{esc(value)}',
        label_style,
    )


def project_title(text, project_style):
    value = normalize(text)
    match = re.search(r"(20\d{2}\.\d{2}\s*-\s*(?:20\d{2}\.\d{2}|至今))\s*$", value)
    date = match.group(1) if match else ""
    title = value[: match.start()].strip() if match else value
    left = Paragraph(esc(title), project_style)
    right_style = ParagraphStyle(
        "ProjectDate",
        parent=project_style,
        fontSize=7.8,
        textColor=TEAL,
        alignment=TA_RIGHT,
    )
    right = Paragraph(esc(date), right_style)
    table = Table([[left, right]], colWidths=[145 * mm, 36 * mm])
    table.setStyle(
        TableStyle(
            [
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                ("LEFTPADDING", (0, 0), (-1, -1), 0),
                ("RIGHTPADDING", (0, 0), (-1, -1), 0),
                ("TOPPADDING", (0, 0), (-1, -1), 0),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 0),
            ]
        )
    )
    return table


def build():
    register_fonts()
    source = Document(SOURCE)
    text = {idx: normalize(p.text) for idx, p in enumerate(source.paragraphs)}
    body, bullet_style, label_style, section_style, project_style = build_styles()

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    doc = BaseDocTemplate(
        str(OUTPUT),
        pagesize=A4,
        leftMargin=14.5 * mm,
        rightMargin=14.5 * mm,
        topMargin=11.5 * mm,
        bottomMargin=17 * mm,
        title="吴志鸿 - Java工程师 / Agent应用开发工程师简历",
        author="吴志鸿",
        subject="个人简历",
    )
    frame = Frame(
        doc.leftMargin,
        doc.bottomMargin,
        doc.width,
        doc.height,
        leftPadding=0,
        rightPadding=0,
        topPadding=0,
        bottomPadding=0,
    )
    doc.addPageTemplates([PageTemplate(id="resume", frames=[frame], onPage=footer)])
    story = []

    kicker_style = ParagraphStyle(
        "Kicker",
        parent=body,
        fontName="ResumeCN-Bold",
        fontSize=7.7,
        leading=9.2,
        textColor=TEAL,
        spaceAfter=1,
    )
    story.append(Paragraph("个人简历  /  SOFTWARE ENGINEERING", kicker_style))

    name_style = ParagraphStyle(
        "Name",
        parent=body,
        fontName="ResumeCN-Bold",
        fontSize=20,
        leading=22,
        textColor=NAVY,
        spaceAfter=0,
    )
    target_style = ParagraphStyle(
        "Target",
        parent=body,
        fontName="ResumeCN-Bold",
        fontSize=10.0,
        leading=12,
        textColor=TEAL,
        alignment=TA_RIGHT,
        spaceAfter=0,
    )
    header = Table(
        [[Paragraph("吴志鸿", name_style), Paragraph("Java工程师 / Agent应用开发工程师", target_style)]],
        colWidths=[64 * mm, 117 * mm],
    )
    header.setStyle(
        TableStyle(
            [
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                ("LEFTPADDING", (0, 0), (-1, -1), 0),
                ("RIGHTPADDING", (0, 0), (-1, -1), 0),
                ("TOPPADDING", (0, 0), (-1, -1), 0),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 2),
            ]
        )
    )
    story.append(header)

    meta_label = ParagraphStyle(
        "MetaLabel", parent=body, fontName="ResumeCN-Bold", fontSize=8.0, leading=9.6, textColor=TEAL
    )
    meta_value = ParagraphStyle("MetaValue", parent=body, fontSize=8.35, leading=9.6, textColor=INK)
    meta = [
        ["姓名", "吴志鸿", "籍贯", "贵州省 遵义市"],
        ["年龄", "23岁", "民族", "汉族"],
        ["手机", "18586746339", "邮箱", "wzhy123zz@163.com"],
    ]
    meta_rows = []
    for row in meta:
        meta_rows.append(
            [
                Paragraph(esc(row[0]), meta_label),
                Paragraph(esc(row[1]), meta_value),
                Paragraph(esc(row[2]), meta_label),
                Paragraph(esc(row[3]), meta_value),
            ]
        )
    meta_table = Table(meta_rows, colWidths=[13 * mm, 68 * mm, 13 * mm, 87 * mm])
    meta_table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, -1), LIGHT),
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                ("LEFTPADDING", (0, 0), (-1, -1), 4),
                ("RIGHTPADDING", (0, 0), (-1, -1), 4),
                ("TOPPADDING", (0, 0), (-1, -1), 2.5),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 2.5),
            ]
        )
    )
    story.extend([meta_table, Spacer(1, 1.3 * mm)])

    story.append(section_heading(text[7], section_style))
    edu_date = Paragraph("2023.09-2027.06", body)
    edu_school_style = ParagraphStyle("EduSchool", parent=body, fontName="ResumeCN-Bold", textColor=NAVY)
    edu_school = Paragraph("沈阳工业大学（一本）", edu_school_style)
    edu_major_style = ParagraphStyle("EduMajor", parent=body, alignment=TA_RIGHT)
    edu_major = Paragraph("电子与计算机工程专业", edu_major_style)
    edu = Table([[edu_date, edu_school, edu_major]], colWidths=[39 * mm, 76 * mm, 66 * mm])
    edu.setStyle(
        TableStyle(
            [
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                ("LEFTPADDING", (0, 0), (-1, -1), 0),
                ("RIGHTPADDING", (0, 0), (-1, -1), 0),
                ("TOPPADDING", (0, 0), (-1, -1), 0),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 0),
            ]
        )
    )
    story.append(edu)
    story.append(label_value("GPA：", text[9].split(":", 1)[1].strip(), label_style))
    courses = text[10] + text[11]
    story.append(label_value("主修课程：", courses.split("：", 1)[1].strip(), label_style))

    story.append(section_heading(text[12], section_style))
    for idx in range(14, 23):
        story.append(bullet(text[idx], bullet_style))

    story.append(section_heading(text[23], section_style))
    for idx in range(25, 36):
        story.append(bullet(text[idx], bullet_style))

    story.append(PageBreak())
    story.append(section_heading(text[37], section_style))
    story.append(project_title(text[38], project_style))
    label, value = split_label(text[39])
    story.append(label_value(label, value, label_style))
    label, value = split_label(text[40])
    story.append(label_value(label, value, label_style))
    story.append(label_value("项目简介：", text[42], label_style))
    story.append(label_value(text[43], "", label_style))
    for idx in range(44, 49):
        story.append(bullet(text[idx], bullet_style))

    story.append(section_heading(text[50], section_style))
    story.append(project_title(text[51], project_style))
    label, value = split_label(text[52])
    story.append(label_value(label, value, label_style))
    label, value = split_label(text[53] + text[54])
    story.append(label_value(label, value, label_style))
    story.append(label_value("项目简介：", text[56], label_style))
    story.append(label_value(text[57], "", label_style))
    for idx in range(58, 63):
        story.append(bullet(text[idx], bullet_style))

    story.append(section_heading(text[63], section_style))
    for idx in range(65, 68):
        story.append(bullet(text[idx], bullet_style))

    doc.build(story)
    print(OUTPUT)


if __name__ == "__main__":
    build()
